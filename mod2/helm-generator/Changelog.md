# Change Log

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [1.0.1]- 2021-10-04

*  [DCAEGEN2-2911] Refactor the code to make it more testable
    - Converted some static methods to instance methods
*  [DCAEGEN2-2911] Distributor throws an error if distribution fails
*  [DCAEGEN2-2917] Add helm repo registry step when initializing the helm client
    - Add "helm repo add ..."
    - Add "helm deployment update" before linting

## [1.0.0]

* Helm chart generator seed code introduction