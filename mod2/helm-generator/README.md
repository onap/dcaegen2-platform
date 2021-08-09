## Instructions for running helm chart generator
version: 1.0.0-SNAPSHOT

1. Must have helm installed.

2. Run `mvn clean package` to build and package the code.

3. Run main class `HelmChartGeneratorApplication` with parameters added to configuration properties, set in the following order:
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