#!/bin/bash

if [ -d "/www/data/nifi-jars" ]; then
    nginx -g "daemon off;"
else
    echo "\"/www/data/nifi-jars\" directory missing"
    echo "You must perform a volume mount to this directory in the container"
    exit 1
fi
