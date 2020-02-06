#!/bin/bash
# ============LICENSE_START=======================================================
# Copyright (c) 2020 AT&T Intellectual Property. All rights reserved.
# ================================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ============LICENSE_END=========================================================

#
# File Name: startOpenDcaeDMD.sh
#
# This is the starting script to start DMD in Open DCAE environment
# It will be called by DCAE controller -- service manager in docker container
# It will start the DMD agent, which will check dmaap.conf to load LCO for all the topics, sub and pub of DR and MR
# Since DMD will not dynamically reload the dmaap.conf, we need to check if the dmaap.conf is updated and reload the agent
# Then it will start the DMD watcher
# It will use the Task file under config/tasks/ to check what watchers to start, both publishers and subscribers
# and needs to have the watcher json file under config/watcher in order for the watchers to start

# to improve this process in 17.10
# if there is no watcher JSON file under config/watcher, generate one, if there is one already, do not overwrite it.
# please note subscriber watcher may not always be the same format, as it may have server side filters, which can not ne auto generated.
# not to start all the watchers, only start when there are files inside the INPUT folder for that topic

# run the vecRun.sh first to setup the env if needed

# try to make this work for both classic controller using dmaap.conf and new controller with KV_Config.json

DMD_HOME=/opt/app/dmd
. ${DMD_HOME}/bin/dmdenv.sh
mkdir -p $DMDLOGDIR

mkdir -p $VCC_HOME/config/watcher
PUBWATCHLIST=$VCC_HOME/config/watcher/dmdpubwatcher.list 	# Watcher expected for publishers
SUBWATCHLIST=$VCC_HOME/config/watcher/dmdsubwatcher.list 	# Watcher expected for subscribers
PUBWATCHLIST_tmp1=$VCC_HOME/config/watcher/dmdpubwatcher.list.tmp1 # hold temp list of watchers from publisher topics
SUBWATCHLIST_tmp1=$VCC_HOME/config/watcher/dmdsubwatcher.list.tmp1 # hold temp list of watchers from subscriber topics
PUBWATCHLIST_tmp2=$VCC_HOME/config/watcher/dmdpubwatcher.list.tmp2 # hold temp list of watchers from publisher topics, sorted and unique
SUBWATCHLIST_tmp2=$VCC_HOME/config/watcher/dmdsubwatcher.list.tmp2 # hold temp list of watchers from subscriber topics, sorted and unique

WORKFILE=/tmp/dmdwatcher_pids.wrk
PIDLIST=/tmp/dmdwatcher_pids.out
LOG=$DMDLOGDIR/dmd_monitor.log

#### define some functions
create_dir() {
    local stus=0

    if [ ! -d "$1" ]; then
      echo "$1 does not exist.. creating.." >> $LOG
      mkdir -p "$1"
          let stus=$?
    else
      echo "$1 exists" >> $LOG
    fi

    if [ $stus -ne 0 ]
    then
      return 1
    else
      return 0
    fi
}

getAgentPid() {
        AGENTPID=$(ps -fe | awk '$2 != "PID" && $0 ~ /-Dtag=DmdAgent / && $0 !~ /grep/ { print $2 }')
}

generate_pub_watcher_config() {
watchername="$1"
mkdir -p $VCC_HOME/config/watcher
touch $VCC_HOME/config/watcher/$watchername-pub.watcher.json
watcherfile=$VCC_HOME/config/watcher/$watchername-pub.watcher.json
mkdir -p $VCC_HOME/data/input/publisher/$watchername
echo "{" >> $watcherfile
echo '  "role"          : "producer",' >> $watcherfile
echo '  "topic"         : "'$watchername'",'  >> $watcherfile
echo '  "id"            : "'$watchername'",'  >> $watcherfile
echo '  "dir"           : "'$VCC_HOME/data/input/publisher/$watchername'",'  >> $watcherfile
echo '  "contenttype"   : "json" '  >> $watcherfile
echo "}" >> $watcherfile
}

checkFileUpdate() {
file="$1"
fingerprintfile="$2"

if [ ! -f $file ]
    then
        echo "ERROR: $file does not exist - aborting" >> $LOG
    	exit 1
fi

# create the md5sum from the file to check
filemd5=`md5sum $file | cut -d " " -f1`

# check the md5 and
# show an error when we check an empty file
if [ -z $filemd5 ]
    then
        echo "The md5sum for $file  is empty " >> $LOG
        exit 1
    else
        # pass silent
        :
fi

# do we have already an saved fingerprint of this file?
if [ -f $fingerprintfile ]
then
	#yup - get the saved md5 $fingerprintfile
    savedmd5=`cat $fingerprintfile`

    # check again if its empty
    if [ -z $savedmd5 ]
    then
    	echo "The savedmd5 in $fingerprintfile  is empty " >> $LOG
        #compare the saved md5 with the one we have now
	else if [ "$savedmd5" = "$filemd5" ]
    then
        # pass silent
        :
    else
    	echo "$file"
    fi
	fi
fi

# save the current md5
echo $filemd5 > $fingerprintfile
}

# dmdwatcher_startup
# PURPOSE: Starts DMD watchers.
# In order to decide what watchers to start, we will check 
# 1. the pub or sub watcher list
# 2. the data input directory if files exist
# 3. if the watcher already started, not to stop or start, do nothing
# 4. check if the watcher config json file exists, if not, generate one, using common format
#
# the watcher will not depend on the taskid

dmdwatcher_startup() {
WATCHERLOGDIR=$DMDLOGDIR/WATCHER
mkdir -p $WATCHERLOGDIR
cd $DMDHOME/bin
			
echo "`date` DMD Watchers starting up." >> $LOG
echo "`date` Start the PUBLISHER watchers first..." >> $LOG
while read PROC
do
	#note: to use the DMD LCO files, it has the -pub or -sub after each topic name
	# remove the -pub to get the topic name first
	
	PROC=`expr ${PROC:0: ${#PROC} - 4 }`	
	pubwatcherconfig=$VCC_HOME/config/watcher/$PROC-pub.watcher.json
	
	# 1. check if the data input folder is there and any files inside
	# update for 18.02, not to use publisher.cfg, so start the publish watchers anyway
	# if [ -d "$VCC_HOME/data/input/publisher/$PROC" ]; then
  	#	if ls $VCC_HOME/data/input/publisher/$PROC/* 1> /dev/null 2>&1; then
    		# echo "files exist", 
    		# step 2: need to check the watcher process if it is already running
    		
    		if ! ps -ef | grep $PROC-pub | grep -i watcher >> $LOG
			then
                # Step3: check if the pub watcher json config file is there, if not, create one                
				if [ ! -s $pubwatcherconfig ]
				then
					echo "`date` generate publisher watcher $PROC config json file" >> $LOG
					generate_pub_watcher_config $PROC
				else
					mkdir -p $VCC_HOME/data/input/publisher/$PROC
					echo "`date` The publisher watcher $PROC config json file exists." >> $LOG
				fi

				watchertype=$(grep \"technology\":101 ${DMD_HOME}/config/overrides/$PROC-pub.json |wc -l)
		        if [[ "$watchertype" = "1" ]]
	    	    then            
					#Run the process for DR publisher watchers
					export WATCHER_INSTANCE=$PROC-pub; nohup dmdwatcher.sh $pubwatcherconfig  > $WATCHERLOGDIR/dmdwatcher.out 2>&1 &                    
                    if ps -ef | grep DmdWatcher.$PROC-pub >> $LOG
                    then                    
                    	echo "`date` Started data file publisher watcher $PROC " >> $LOG
                    else 
                    	echo "`date` data file publisher watcher $PROC did not start successfully, check watcher log"  >> $LOG
                    fi                    
        		fi 
        	       
				#Run the process for MR publisher watchers
				watchertype=$(grep \"technology\":206 ${DMD_HOME}/config/overrides/$PROC-pub.json |wc -l)
    	    	if [[ "$watchertype" = "1" ]]
        		then 
                    export WATCHER_INSTANCE=$PROC-pub; nohup dmdMsgWatcher.sh $pubwatcherconfig  > $WATCHERLOGDIR/dmdwatcher.out 2>&1 &                    
                    if ps -ef | grep DmdMsgWatcher.$PROC-pub >> $LOG
                    then                    
                    	echo "`date` Started message publisher watcher $PROC " >> $LOG
                    else 
                    	echo "`date` message publisher watcher $PROC did not start successfully, check watcher log"  >> $LOG
                    fi
				fi   
			else
				echo "`date` $PROC-pub is already running, nothing to do, exiting" >> $LOG
			fi		
		#else
    	#	echo "`date` There are no files in $PROC for publishing, no need to startup watcher" >> $LOG
		#fi
	#fi
done <$PUBWATCHLIST

echo "`date` Start the SUBSCRIBER watchers now ..." >> $LOG
while read PROC
do
	# check if the dmd config file for the watcher exists
	# NOTE: the subscriber watcher json must be created already, not auto generated
	# The trigger process cannot be auto created.
	# all subscriber watchers will be started, as it will wait for data to subscribe.
	
	#note: to use the DMD LCO files, it has the -pub or -sub after each topic name
	# remove the -sub to get the topic name first	
	PROC=`expr ${PROC:0: ${#PROC} - 4 }`
	
	subwatcherconfig=$VCC_HOME/config/watcher/$PROC-sub.watcher.json
	if [ -s $subwatcherconfig ]
	then
		if ! ps -ef | grep $PROC-sub | grep -i watcher >> $LOG
		then
			#check if the folder from the watcher config file, and create the directory is needed
			WATCHERDIR=$(grep "dir" $subwatcherconfig | cut -d ":" -f 2 | sed -e 's/"\(.*\)",/\1/'|tr -d '\"'|tr -d ' ')
			echo "`date` Creating the subscriber watcher dir: $WATCHERDIR " >> $LOG
			create_dir $WATCHERDIR
                	
			#Run the process for DR subscriber watchers
			#watchertype=$(grep $PROC $VCC_HOME/config/subscriber/subscriber.cfg |cut -d "|" -f 3 | cut -c1-2 )
			
			watchertype=$(grep \"technology\":101 ${DMD_HOME}/config/overrides/$PROC-sub.json |wc -l)
	        if [[ "$watchertype" = "1" ]]
	        then            
                    export WATCHER_INSTANCE=$PROC-sub; nohup dmdwatcher.sh $subwatcherconfig  > $WATCHERLOGDIR/dmdwatcher.out 2>&1 &
                    
                    if ps -ef | grep DmdWatcher.$PROC-sub >> $LOG
                    then                    
                    	echo "`date` Started data file subscriber watcher $PROC " >> $LOG
                    else 
                    	echo "`date` data file subscriber watcher $PROC did not start successfully, check watcher log"  >> $LOG
                    fi                    
        	fi 
        	       
			#Run the process for MR subscriber watchers
			watchertype=$(grep \"technology\":206 ${DMD_HOME}/config/overrides/$PROC-sub.json |wc -l)
    	    if [[ "$watchertype" = "1" ]]
        	then 
                    export WATCHER_INSTANCE=$PROC-sub; nohup dmdMsgWatcher.sh $subwatcherconfig  > $WATCHERLOGDIR/dmdwatcher.out 2>&1 &
                    
                    if ps -ef | grep DmdMsgWatcher.$PROC-sub >> $LOG
                    then                    
                    	echo "`date` Started message subscriber watcher $PROC " >> $LOG
                    else 
                    	echo "`date` message subscriber watcher $PROC did not start successfully, check watcher log"  >> $LOG
                    fi
			fi              
		else
			echo "`date` $PROC-sub is already running, nothing to do, exiting" >> $LOG
		fi
	else 
		echo "`date` $PROC is in the task, but there is no $PROC-sub watcher configuration, skip this task" >> $LOG		
	fi
done <$SUBWATCHLIST

echo "`date` DMD Watchers startup completed." >> $LOG
#exit
}

# dmdwatcher_shutdown
# PURPOSE: Kills DMD watchers with extreme prejudice.

dmdwatcher_shutdown() {
# initialize PIDs
>$PIDLIST

echo "`date` Shutting down DMD Watchers." >> $LOG
        ps -ef | grep tag=Dmd | grep Watcher | grep -v grep > $WORKFILE.$$
        awk '{ print $2 ; }' $WORKFILE.$$ > $PIDLIST
                while read PID
                do
                        kill -9 $PID
                        echo "`date` Killing PID $PID." >> $LOG
                done <$PIDLIST
echo "`date` DMD Watchers shutdown successfully." >> $LOG
echo "`date` Removing temporary files." >> $LOG
        rm -f $WORKFILE.$$
        rm -f $PIDLIST
echo "`date` Cleanup watcher completed, exiting." >> $LOG
#exit

}

#### main process
# DMD AGENT START
#####

# initialize log
>$LOG
echo "`date` DMD process starting, to check DMD agent and watcher status...." >> $LOG

mkdir -p /opt/app/dcae-certificate
mkdir -p ${DMD_HOME}/metrics
mkdir -p ${DMD_HOME}/config/overrides

aafcertmanPassword=/opt/app/aafcertman/.password
aafcertmanKeystore=/opt/app/aafcertman/keystore.jks

if [ ! -f "$aafcertmanPassword" ] || [ ! -f $aafcertmanKeystore ]
then
         echo "`date` keystore password $aafcertmanPassword or keystore $aafcertmanKeystore file is not found. This may be an issue if it is needed in DMaaP Subscriber."  >> $LOG
fi
 
newDmaapConfFile=${VCC_HOME}/tmp/configManager/output/KV_Config.json

if [ ! -f $newDmaapConfFile ]
then
	echo "`date` DMD process cannot start, there is no dmaap conf files for EOM" >> $LOG
	exit 1
fi

# check to see if dmaap.conf is updated, in case the dmaap is updated dynamically
if [ -s $aafcertmanPassword ]
then
	dmaapUpdated=$(checkFileUpdate "$aafcertmanPassword" "$VCC_HOME/config/watcher/.aafcertmanPassword.md5") || exit 1
elif [ -s $aafcertmanKeystore ]
then
	dmaapUpdated=$(checkFileUpdate "$aafcertmanKeystore" "$VCC_HOME/config/watcher/.aafcertmanKeystore.md5") || exit 1
elif [ -s $newDmaapConfFile ]
then
	dmaapUpdated=$(checkFileUpdate "$newDmaapConfFile" "$VCC_HOME/config/watcher/.kv_config.md5") || exit 1
fi

## comment out below, there is no need to check, we need to start the watchers according to the existence of files in the pub or sub data directory
#watcherListUpdated=false
#if [ -s $WATCHLIST ]
#then
#	if cmp $WATCHLIST_tmp2 $WATCHLIST > /dev/null
#	then 
#		echo "watcher list in the config file not changed" >> $LOG
#	else
#		watcherListUpdated=true
#		cat $WATCHLIST_tmp2 > $WATCHLIST
#	fi
#else
#	echo "watcher list does not exist, creating the new one" >> $LOG
#	taskUpdated=true
#	cat $WATCHLIST_tmp2 > $WATCHLIST
#fi

getAgentPid
if [ -n "$AGENTPID" ]
then
	echo "`date` DMD Agent is already started " >> $LOG	
#	if [ ! -z "$dmaapUpdated" ] || [ $taskUpdated =  true ]
	if [ ! -z "$dmaapUpdated" ] 
	then
		dmdwatcher_shutdown
		sleep 5

		nohup ${DMD_HOME}/bin/dmdagent.sh stop >> $LOG 2>>$LOG &
		sleep 10

		echo "`date` DMD Agent is forced to stop, then start the Agent again... " >> $LOG
		# now start DMD all new again
		nohup ${DMD_HOME}/bin/dmdagent.sh start >> $LOG 2>>$LOG &
		sleep 20
	fi
else		
	echo "`date` before starting the Agent, encrypt the password " >> $LOG			
	${DMD_HOME}/bin/dmdencryptpassword.sh >> $LOG 2>>$LOG &
	sleep 1
	echo "`date` DMD Agent is not running, start the Agent first " >> $LOG		
	nohup ${DMD_HOME}/bin/dmdagent.sh start >> $LOG 2>>$LOG &
	sleep 20	
fi

# after DMD agent is up and created the LCO, generate the WATCHER config json files using the LCO
if [ -s $PUBWATCHLIST_tmp1 ]
then
	rm -f $PUBWATCHLIST_tmp1
fi

if [ -s $SUBWATCHLIST_tmp1 ]
then
	rm -f $SUBWATCHLIST_tmp1
fi

touch $PUBWATCHLIST
touch $SUBWATCHLIST

touch $PUBWATCHLIST_tmp1
touch $SUBWATCHLIST_tmp1

#if [-s $classicDmaapConfFile ]
#then
#	echo "`date` DMD checks dmaap.conf file, it exists, get the streamid ...." >> $LOG
#	cat $classicDmaapConfFile | grep -v "#" | grep -e "dmaapAction" -e "dmaapStreamId" | awk 'NR%2{printf "%s ",$0;next;}1' |grep -i "publish" |cut -d "," -f 2 |cut -d ":" -f2 |sed 's/ //g' |sed -e 's/^"//' -e 's/"$//' >> $PUBWATCHLIST_tmp1
#elif [-s $newDmaapConfFile ]
#	echo "`date` DMD checks KV_Config.json file, it exists, get the config_key ...." >> $LOG
#	cat $newDmaapConfFile |grep -e "dmaapAction" -e "dmaapStreamId" | awk 'NR%2{printf "%s ",$0;next;}1' |grep -i "publish" |cut -d "," -f 2 |cut -d ":" -f2 |sed 's/ //g' |sed -e 's/^"//' -e 's/"$//' >> $PUBWATCHLIST_tmp1
#fi

# pubWatcherFile=$VCC_HOME/config/publisher/publisher.cfg
# note: in 18.02, we switch to use DMaaP Global TOPIC not streamid, as the config_key
#if [ -s $pubWatcherFile ] 
#then
#	grep . $pubWatcherFile | grep -v "#" |cut -d "|" -f 2  >> $PUBWATCHLIST_tmp1
#	grep . $pubWatcherFile | grep -v "#" |cut -d "|" -f 3  >> $PUBWATCHLIST_tmp1
#fi

#subWatcherFile=$VCC_HOME/config/subscriber/subscriber.cfg
#if [ -s $subWatcherFile ] 
#then
#	grep . $subWatcherFile | grep -v "#" |cut -d "|" -f 2  >> $SUBWATCHLIST_tmp1
#	grep . $subWatcherFile | grep -v "#" |cut -d "|" -f 3  >> $SUBWATCHLIST_tmp1	
#fi

find ${DMD_HOME}/config/overrides/*.json | sed -e 's/.*\///g' | grep -i "\-pub.json" | sed 's/.[^.]*$//g'  >> $PUBWATCHLIST_tmp1
find ${DMD_HOME}/config/overrides/*.json | sed -e 's/.*\///g' | grep -i "\-sub.json" | sed 's/.[^.]*$//g'  >> $SUBWATCHLIST_tmp1

if [ -s $PUBWATCHLIST_tmp1 ] 
then
	cat $PUBWATCHLIST_tmp1 |sort -u > $PUBWATCHLIST_tmp2
fi

if [ -s $SUBWATCHLIST_tmp1 ] 
then
	cat $SUBWATCHLIST_tmp1 |sort -u > $SUBWATCHLIST_tmp2
fi

if [ -s $PUBWATCHLIST_tmp2 ] 
then
	grep -iv OPENDCAE_DMD_METRICS $PUBWATCHLIST_tmp2 > $PUBWATCHLIST
fi
if [ -s $SUBWATCHLIST_tmp2 ] 
then
	grep -iv OPENDCAE_DMD_METRICS $SUBWATCHLIST_tmp2 > $SUBWATCHLIST
fi

# DMD WATCHER START
getAgentPid
if [ -n "$AGENTPID" ]
then
	echo "`date` DMD Agent is running now, startup the watchers..." >> $LOG	
	dmdwatcher_startup
fi

rm -f $PUBWATCHLIST_tmp1  $PUBWATCHLIST_tmp2 $SUBWATCHLIST_tmp1  $SUBWATCHLIST_tmp2
echo "`date` DMD agent and subscriber watcher startup completed." >> $LOG
exit 0
