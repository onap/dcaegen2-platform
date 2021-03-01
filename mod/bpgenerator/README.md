# Blueprint Generator 

Blueprint Generator is a java-based project used to generate a cloudify blueprint yaml from a component spec json file.

It can be used either:
- as a standalone application by directly using *onap-executable/target/blueprint-generator-onap-executable-1.7.3-SNAPSHOT.jar*
- as a Spring library

# Instructions for building the tool locally
- Change directory into the root directory of the project (where the pom is located)
- Run the command: `mvn clean install`
- This will create jar files

# Instructions for running BlueprintGenerator standalone:
Base command to run BlueprintGenerator:
```bash
java -jar onap-executable/target/blueprint-generator-onap-executable-1.7.3-SNAPSHOT.jar app ONAP
```
## Instructions for component blueprints:
Run the program on the command line with the following options:
- -i OR --component-spec: The path of the ONAP Blueprint INPUT JSON SPEC FILE (Required)
- -p OR --blueprint-path: The path of the ONAP Blueprint OUTPUT where it will be saved (Required)
- -n OR --blueprint-name: The NAME of the ONAP Blueprint OUTPUT that will be created (Optional)
- -t OR --imports: The path of the ONAP Blueprint IMPORT FILE (Optional)
- -o OR --service-name-override: The Value used to OVERRIDE the SERVICE NAME of the ONAP Blueprint  (Optional)
- -d OR --dmaap-plugin: The option to create an ONAP Blueprint with DMAAP Plugin included (Optional)

it will look like this:

```bash
java -jar onap-executable/target/<JAR Filename>.jar app ONAP -i componentspec -p OutputBlueprintPath  -n Blueprintname -d
```

This command will create a blueprint from the component spec. The blueprint file name will be called Blueprintname.yaml and it will be in the directory OutputBlueprintPath. The blueprint will also contain the DMaaP plugin. 

### Extra information:
- The component spec must be of the same format as stated in the onap [readthedocs](https://docs.onap.org/projects/onap-dcaegen2/en/latest/sections/design-components/component-specification/component-type-docker.html) page 
- If the tag says required then the program will not run without those tags being there
- If the tag says optional then it is not necessary to run the program with those tags
- If you do not add a -n tag the blueprint name will default to what it is in the component spec
- If the directory you specified in the -p tag does not already exist the directory will be created for you
- The -t flag will override the default imports set for the blueprints. Below you can see example content of the import file:
```yaml
imports:
  - https://www.getcloudify.org/spec/cloudify/4.5.5/types.yaml
  - plugin:k8splugin?version=3.6.0
  - plugin:dcaepolicyplugin?version=2.4.0
```

## Instructions for policy models
In order to use BlueprintGenerator for policy models option: *-type policycreate* needs to be provided to the base
command. Other available options:
- -i: The path to the JSON spec file (required)
- -p: The output path for all of the models (required)

it will look like this:
                   
```bash
java -jar onap-executable/target/<JAR Filename>.jar app ONAP -type policycreate -i componentspec -p OutputPolicyPath
```

This command will create a directory called models and put the policy models created from the component spec given in that directory. (A component spec may generate multiple policy models)

# Instructions for using BlueprintGenerator as a library
To use BlueprintGenerator you need to import the following artifact to your project:
```xml
<dependency>
    <groupId>org.onap.dcaegen2.platform.mod</groupId>
    <artifactId>blueprint-generator-onap</artifactId>
    <version>1.7.3</version>
</dependency>
```
In order to see how to use the library in detail please see file:
*bpgenerator/onap-executable/src/main/java/org/onap/blueprintgenerator/BlueprintGeneratorMainApplication.java*
