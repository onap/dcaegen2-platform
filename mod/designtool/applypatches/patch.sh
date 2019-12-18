#!/bin/bash

set -euf -o pipefail

cd ../nifi

if [ ! -f ../patches-applied ]
then
	echo Applying Design Tool Patches
	git am ../design-tool-changes.patch
	date >../patches-applied
	echo Patches Applied
fi
