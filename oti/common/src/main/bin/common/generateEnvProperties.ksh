################################################################
#
# Script: generateEnvProperties.ksh
# Created By: Brian Veigl           Date: 6/13/2017
# Revised By: Brian Veigl           Date: 
#
# Description:
# This script will take a java "property file" which contains the property variables which 
# the java process will need to use.  The right hand side of the = can contain environment
# variables.  Tis will allow you to use and variables defined in bin/common/vcc_env.  
# When a "start process" script is executed it will dot this file as the first line of the 
# script.  This script will take the "property file" and generate a "generated property file" 
# which evaluates the variables which were used on the right side of the = and it will 
# include all variables which were defined in vcc_env.  
#
#
# Example:
# script startDecoder.ksh will include as its first line
#   . $VCC_HOME/bin/common/generateEnvProperties.ksh <full path to "property file"> <full path to "generated property File"> 
#   . $VEC_HOME/bin/common/generateEnvProperties.ksh /opt/app/vcc/config/uspDecode/env.properties /opt/app/vcc/config/usp/cdr/ccf/env.properties
#
#
#
################################################################
. $VCC_HOME/bin/common/vcc_env

fromFile=$1
toFile=$2

# generate the property file by first evaluating each line in the file.  This will
# resolve any variables on the right side of the =
>$toFile
while read line
do
	eval echo $line >> $toFile
done < $fromFile

# Concatenate what is in your envirnoment to the "generate property" file by
# Removing entiries from your enviroment which should not be used by anyone
# Then concatenating what is left to your "generated property" file which was created above. 
env | grep "=" | grep -v "^_" | grep -v "^LS_COLORS=" | grep -v "^PS1=" | grep -v "^PS2=" | grep -v "^A__z=" >> $toFile
sort $toFile -o $toFile
