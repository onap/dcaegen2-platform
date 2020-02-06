#!/bin/ksh

function _die {
        print "FATAL: $1\n"
        exit 2;
}

#
# MAIN
#
while getopts "i:(install-dir)d:(env):" optchar
do
    case $optchar in
    
    	d) DCAE_ENV=$OPTARG ;;

        i) INSTALLDIR=$OPTARG ;;

    esac
done

shift $(($OPTIND - 1 ))

[ -z "$DCAE_ENV" ] && _die "Must specify a DCAE environment"

JAR=$(ls lib/dti-*-final.jar)
[ ! -f $JAR ] && _die "Cannot find $JAR: $!"

TARGET="${INSTALLDIR}"
print "Installing dti ...\n"
print "Copying files into place ... "
find . ! -name install*.sh -print | cpio -pdumv $TARGET
print "FINISHED\n"

JARBASE=$(basename $JAR)
cd ${INSTALLDIR}/lib || _die "Cannot chdir to ${INSTALLDIR}/lib: $!"
[ ! -f $JARBASE ] && _die "$JARBASE is not in the working directory: $!"

[ -h dti.jar ] && rm -f dti.jar
print "Sym linking $JARBASE to dti.jar ... \c"
ln -s $JARBASE dti.jar
print "FINISHED\n"

ESCINSTALLDIR=$(echo $INSTALLDIR | sed -e 's/\//\\\//g')
JAVA_HOME="$TARGET/java"
ESCJAVA_HOME=$(echo $JAVA_HOME | sed -e 's/\//\\\//g')

cd ${INSTALLDIR}/bin|| _die "Cannot chdir to ${INSTALLDIR}/bin: $!"
INSTALLEDFILES=$(ls -1 | grep -v logs)

print "Replacing tokens ..."
perl -p -i -e "
	 s/<installdir>/$ESCINSTALLDIR/g;
     s/<java_home>/$ESCJAVA_HOME/g;
     s/<dcae_env>/$DCAE_ENV/g;
     " $INSTALLEDFILES

print "Creating dirs ..."
mkdir -p /home/netman/common/dti1710/temp_config
chmod 755 /home/netman/common/dti1710/temp_config
mkdir -p /home/netman/common/dti1710/java/bin
mkdir -p /home/netman/common/dti1710/keystore
chmod 755 /home/netman/common/dti1710/keystore
cp /home/netman/SSL-AAI/*.p12 /home/netman/common/dti1710/keystore

print "Post install steps ..."
cd /home/netman/common/dti1710/config
chmod 755 dti_aai_feed.properties
mv vm_pattern_map_d1.properties vm_pattern_map.properties
mv vnf_pattern_map_d1.properties vnf_pattern_map.properties
mv dti_active_task_d1.properties dti_active_task.properties
 
     
print "FINISHED"
