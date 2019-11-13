# Acumos Model DCAE Component

This package is a service that converts Acumos models into ONAP DCAE components.

This operates in 2 modes:

- In command line mode, the catalogs and Acumos instance providing the models
  to be converted are identified in the configuration file, and the service
  exits once they have been processed.
- In web UI mode, a TCP/IP port to listen on is identified in the
  configuration file, this port provides access to a web UI for identifying
  the models to be converted, and the service continues to handle requests
  after processing models.

# Unit Testing

    tox
    open htmlcov/index.html

# Runtime Pre-Requisites

- An ACUMOS instance from which to pull models.
- A PEM format file containing the unencrypted private key, certificate, and
  any necessary intermediate certificate(s) used to identify this tool to
  the above ACUMOS instance.
- Any required DNS/Firewall setup such that this tool will be able to connect
  to the Federation Gateway of the ACUMOS instance.
- The tool must be able to access the Docker daemon
- The Docker daemon must have connectivity to the Docker registry used by ONAP.
- The credentials the Docker daemon will use to write the Docker registry must
  be known.
- The tool must be able to access the DCAE onboarding service API
- The credentials the tool will use to invoke the DCAE onboarding service must
  be known.
- The tool must be able to access web sites containing the DCAE data format and
  component JSON schemas.
- "pip install nodeenv", "nodeenv -p", "npm install --global protobuf-jsonschema",
  and "pip install aoconversion", or equivalent must be run.
- The DCAE user who will "own" the loaded data formats and components must be
  known.
- If the certificate chain of the ACUMOS instance's server certificate does not
  lead to a well known certificate authority, then a PEM format file containing
  the appropriate CA certificate must be available.
- A configuration file, in YAML format, containing the following keys must be
  available.

  dcaeurl - The base URL for the DCAE component and data format schemas.  For
    example, https://git.onap.org/dcaegen2/platform/cli/plain.
  dcaeuser - The DCAE user who will "own" the loaded data.
  onboardingurl - The URL for accessing the onboarding service.
  onboardinguser - The user ID for accessing the onboarding service.
  onboardingpass - The password for accessing the onboarding service.
  port - (required in web UI mode, not allowed in command-line mode) The TCP/IP
    port to listen on in web UI mode.
  acumosurl - (required in command-line mode) The URL for the Federation
    Gateway of the ACUMOS instance.
  certfile - The file path for the PEM file containing the private key, etc.
  dockerhost - (optional) The URL for the docker host.  By default,
    unix:///var/run/docker.sock.
  dockerregistry - The host:port for the ONAP docker registry.
  dockeruser - The user ID for uploading images to the docker registry.
  dockerpass - The password for uploading images to the docker registry.
  certverify - (optional) The PEM file containing the CA certificate.  By
    default, a standard set of certificate authorities are recognized.
  tmpdir - (optional) A directory in which to work.  By default,
    temporary files are put in /var/tmp/aoadapter.
  interval - (optional) The number of seconds between scans of the ACUMOS
    instance.  By default, 900 seconds (15 minutes).
  catalogs - (optional) a list of catalog IDs or catalog names to load.  By
    default, all catalogs are loaded.

  Note: The values of onboardingpass and dockerpass can either be the literal
  passwords to be used or, if they begin with "@" they can specify the paths
  to files containing the passwords.

# Running

Assuming the name of the above configuration file is "config.yaml,"

    acumos-adapter config.yaml
