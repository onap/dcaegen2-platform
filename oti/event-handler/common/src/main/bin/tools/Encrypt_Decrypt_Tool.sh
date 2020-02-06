#!/bin/bash

#############################################################################
#
#  NAME
#   Encrypt_Decrypt_Tool.sh - Encrypts/Decrypts a string using AES , and Base64 encoding/decoding.
#
#  AUTHOR(S)
#   Vinodh Pemmasani; vp663p@att.com
#
#  SYNOPSIS
#   eEncrypt_Decrypt_Tool.sh [-e|-d] <string>
#
#   -e encrypt a specified string
#
#   -d decrypt the specified string
#
#   Example(s):
#   Encrypt_Decrypt_Tool.sh -e mycommstring/mypasswd
#
#   SETUP:
#       This requires the JAVA_HOME and the ENCRYPTER_HOME to be setup before use,
#        to point to the location of the java location and the location of the
#       encrypter jar respectively.
#############################################################################################
if [ "$HOME" = "" ]
then
        echo "Environment variable HOME is not defined - exiting" >&2
        exit 1
fi

. $VCC_HOME/bin/common/vcc_env

if [ "$JAVA_HOME" = "" ]
then
        echo "Error: Environment variable JAVA_HOME not set, exiting"  >&2
        exit 1
fi

export ENCRYPTERJAR=$VCC_HOME/lib/common.jar
export PATH=$JAVA_HOME/bin:$PATH
export CLASSPATH=$CLASSPATH:.:$ENCRYPTERJAR
##########################################################################################

SCRIPT=`basename ${0}`

USAGE="USAGE: ${SCRIPT} [-e|-d] <string>"


OPT=$1 #option
STRINGARG=$2 #string

case ${OPT} in
        -e)
                [ -z $STRINGARG ] && { echo " String argument is missing"; echo $USAGE; exit 1; }
                ;;
        -d)
                [ -z $STRINGARG ] && { echo " String argument is missing"; echo $USAGE; exit 1;}
                ;;
        *)
                echo ""
                echo "Invalid arguments!"
                echo "$USAGE"
                exit 1
                ;;
esac

java com.att.vcc.common.encrypt.EncryptString ${OPT} ${STRINGARG}

exit 0