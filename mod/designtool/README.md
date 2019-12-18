# DCAE MOD's Design Tool

DCAE MOD's DCAE design tool is a fork of Nifi's `support/nifi-1.9.x` branch with a series of commits made by the DCAE MOD team.

In this directory you will find a git submodule that points to the public Nifi repo and a git patch file.  In order to get setup for development and build, you must first pull in the submodule files by invoking the following from the top-level of this project where the `.gitmodules` file exists:

```
$ git submodule update
```

Then apply the patch file onto the submodule:

```
$ cd mod/designtool/nifi
$ git am ../design-tool-changes.patch
```

After all the commits have been applied, the working files in the `nifi` directory is the DCAE MOD design tool project and ready for development and build.

## Development

Commit code changes within the `nifi` directory while inside the `nifi` directory.  Then you must generate a new patch file and replace the existing patch file that exists at the `designtool` superproject level.

```
$ git format-patch --stdout support/nifi-1.9.x > ../design-tool-changes.patch
```

Then from the superproject level, commit changes to the patch file but make sure **you do not** commit the staged changes to the `nifi` directory.

Note that the patch file changes will always be very noisy and have differences outside of your control e.g. commit ID changes and white spaces differences.

## Build

Doing a maven build will produce a project directory that is zipped up that contains configs, nar files, war files and can take a while so try to parallelize the process:

```
$ mvn -T 2.0C clean install -DskipTests
```

To build the Docker image, you must first do the maven build then do:

```
$ cd nifi-docker/dockermaven
$ mkdir target
$ cp ../../nifi-assembly/target/nifi-1.9.3-SNAPSHOT-bin.zip target/
$ cp ../..//nifi-toolkit/nifi-toolkit-assembly/target/nifi-toolkit-1.9.3-SNAPSHOT-bin.zip target/
$ docker build --build-arg NIFI_BINARY=./target/nifi-1.9.3-SNAPSHOT-bin.zip \
    --build-arg NIFI_TOOLKIT_BINARY=./target/nifi-toolkit-1.9.3-SNAPSHOT-bin.zip \
    --build-arg NIFI_VERSION=1.9.3-SNAPSHOT \
    -t nifi-mod .
```
