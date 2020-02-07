# DCAE MOD's Design tool

## License

Copyright 2020 AT&T Intellectual Property. All rights reserved.

This file is licensed under the CREATIVE COMMONS ATTRIBUTION 4.0 INTERNATIONAL LICENSE

Full license text at https://creativecommons.org/licenses/by/4.0/legalcode


## Description

DCAE MOD's DCAE design tool is based on Nifi 1.9.2 with modifications
made by the DCAE MOD team.

## Development

The designtool-web module contains the modified versions of Nifi files, along
with a Dockerfile and a script (sh/applypatches.sh) for replacing them in
the nifi Docker image, to produce the design tool Docker image.

If the set of modified files changes, then the Dockerfile, the script, and
potentially the pom.xml may require changes.

In particular, note that the Nifi build creates 2 "bin" files, one for nifi
itself and the other for the nifi-toolkit, which are expanded into separate
directories in the nifi image.  Contained in the "bin" files are "nar" files,
which contain "jar" and "war" files.  And, inside the nifi-web-ui "war" file
are several "-all.js" and "-all.css" files, containing minified aggregations
of the various js and css source files.  The applypatches script needs to
appropriately patch these nar, war, jar, all.js, and all-css files (some of
which also have gzipped versions).

The nifi-war-to-jar module builds a jar archive from the classes in the
nifi-web-api war archive, that the modified files in the designtool-web
module can be compiled against.
