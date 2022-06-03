# Change Log

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/) 
and this project adheres to [Semantic Versioning](http://semver.org/).

## [1.3.3] - 2022-06-03
- [DCAEGEN2-3087] Switch helmgen-core version to 1.0.4 (includes fix for log path)

## [1.3.2] - 2022-01-04
- [DCAEGEN2-3028] Change version format to MMddHHmm (removed YY)
- [DCAEGEN2-3008] Update helmgen-core from snapshot to released version
- [DCAEGEN2-3088] Set spring-boot-maven-plugin to 2.6.2
- [DCAEGEN2-3052] Update helmgen-core to 1.0.3 (includes vulnerability fixes for J)

## [1.3.1] - 2021-10-13
- [DCAEGEN2-2936] Update helmgen-core to 1.0.2
- [DCAEGEN2-2936] Update helm version to 3.5.4

## [1.3.0] - 2021-09-16
- [DCAEGEN2-2694] Integrate helm chart generator
    - create a switch to toggle between blueprint and helm based deployment types
- [DCAEGEN2-2805] Update commons-io version to 2.8.0

## [1.2.3]
- Update BPGenerator 1.7.3
  - Update default k8splugin import
  - Externalize resource limits defaults for generated blueprints

## [1.2.2]
- Update SpringFox dependencies to version 3.0.0
  - Swagger documentation URL changed from: /swagger-ui.html to: /swagger-ui/index.html
