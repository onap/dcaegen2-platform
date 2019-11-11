# Genprocessor

This project is a tool to experiment with generating a Nifi Processor POJO from a DCAE component spec.

Environment variables needed to run the app:

For generating -

`GENPROC_WORKING_DIR` - Full file path to the directory where you will generate class files to and ultimately build the jar to distribute
`GENPROC_ONBOARDING_API_HOST` - Onboarding API host URL
`GENPROC_PROCESSOR_CLASSFILE_PATH` - Path to the DCAEProcessor class file

For loading -

`GENPROC_JAR_INDEX_URL` - URL to the index.json for DCAE processor jars

## Build

NOTE: You need a specific version of the `nifi-api` jar that contains the class `BaseDCAEProcessor`.

Command to build and to copy dependencies into `target/dependency` directory:

```
mvn clean package dependency:copy-dependencies
```

## Run - Generate jars

This will pull all component specs from onboarding API and for each component:

* A class file is generated for a new DCAEProcessor class
* Write metadata into META-INF directory
* Copy a copy of the DCAEProcessor class file
* Package up into a jar 

Command to run:

```
java -cp "target/genprocessor-1.0.1.jar:target/dependency/*" sandbox.App gen
```

### More about what goes into META-INF

#### Processor manifest

Note the META-INF directory which contains:

```
$ tree META-INF/
META-INF/
└── services
    └── org.apache.nifi.processor.Processor
```

If you don't have the above in your `GENPROC_TARGET_DIR`, then:

```
$ mkdir -p META-INF/services
$ touch META-INF/services/org.apache.nifi.processor.Processor
```

Open `META-INF/services/org.apache.nifi.processor.Processor` and write the full class name for each generated processor on a separate line.

#### MANIFEST.MF

Write the `MANIFEST.MF` in a file that's arbitrarily named (mymanifest for example).  The content should look like:

```
$ cat mymanifest 
Manifest-Version: 1.0
Id: dcae-ves-collector
Version: 1.5.0
Group: org.onap.dcae
```

## Run - Load jars

This will load all jars listed on an index page and for each jar will do a class load and quick test.

Command to run:

```
java -cp "target/genprocessor-1.0.1.jar:target/dependency/*" sandbox.App load
```