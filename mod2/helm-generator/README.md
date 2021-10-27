## Instructions for running helm chart generator
version: 1.0.2-SNAPSHOT

1. Must have helm installed.

2. Run `mvn clean package` to build and package the code.

3. Discover helmchartgenerator-cli-<version>.jar under helmchartgenerator-cli/target

4. Override the default values for Chart Museum APIs for chart distribution by setting the following ENV variables:
    CHARTMUSEUM_BASEURL -> Base URL along with a port of Chart Museum (e.g "http://chartmuseum:8080")
                           (Note: the port is only needed if it's not the standard 80 for http or 443 for https.)
    CHARTMUSEUM_AUTH_BASIC_USERNAME -> Username for basic auth
    CHARTMUSEUM_AUTH_BASIC_PASSWORD -> Password for basic auth

5. Run the jar with these parameters set in the following order:
    1. Spec file location
    2. Chart directory location
    3. Output directory location
    4. Component spec schema (Optional)
    5. --distribute flag (Optional)
    6. --help (to see usage)

    Arguments expected as: `<somePath/spec.json>  <somePath/chartDirectory> <somePath/outputDirectory> <somepath/specSchema.json> --distribute`

    Test files currently included in project:
        - Spec file: `helm-chart-generator\src\test\input\specs\ves.json`
        - Charts Directory: `helm-chart-generator\src\test\input\blueprint` 
        
        gi  