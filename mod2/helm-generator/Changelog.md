# Change Log

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [1.0.2]- 2021-11-05
*  [DCAEGEN2-2936] Convert streams_publishes and streams_subscribes json strings under applicationConfig to map
*  [DCAEGEN2-2948] Spec schema changes: Change Cluster to ClusterIP, make policy-id as required field
*  [DCAEGEN2-2949] Add useCmpv2Certificates: true and include certificate.yaml (add-on)
*  [DCAEGEN2-2950] Remove hyphens from a component name under postgres-config section
*  [DCAEGEN2-2951] Enhance Readme file: add environment variables info
*  [DCAEGEN2-2960] update dependencies to 9.x-0. Add ServiceAccount.nameOverride substitution check.

## [1.0.1]- 2021-10-04

*  [DCAEGEN2-2911] Refactor the code to make it more testable
    - Converted some static methods to instance methods
*  [DCAEGEN2-2911] Distributor throws an error if distribution fails
*  [DCAEGEN2-2917] Add helm repo registry step when initializing the helm client
    - Add "helm repo add ..."
    - Add "helm deployment update" before linting

## [1.0.0]

* Helm chart generator seed code introduction
