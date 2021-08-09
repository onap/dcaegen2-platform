## Instructions for running helm chart generator

1. Must have helm installed.

2. Run `mvn clean package` to build and package the code.

3. Run main class `HelmChartGeneratorApplication` with parameters added to configuration properties, set in the following order:
    1. Spec file location
    2. Chart directory location
    3. Output directory location

    Arguments expected as: `<somePath/spec.json>  <somePath/chartDirectory> <somePath/outputDirectory>`

    Test files currently included in project:
        - Spec file: `helm-chart-generator\src\test\input\specs\ves.json`
        - Charts Directory: `helm-chart-generator\src\test\input\blueprint`