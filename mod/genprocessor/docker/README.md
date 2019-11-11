# Genprocessor: Docker

`http` - http server that serves up the DCAE Nifi jars as files under the path `/nifi-jars`
`job` - background job that continuously polls the onboarding API for components and generates jars from components

The usage here will assume the use of a docker volume to persist data.

Create a volume:

```
docker volume create genprocessor
```

## job

Build:

```
$ cd ../
$ docker build -t genprocessor-job -f docker/job/Dockerfile .
```

Run:

```
docker run -v genprocessor:/work -e GENPROC_ONBOARDING_API_HOST=http://some-hostname/onboarding -d genprocessor-job
```

NOTE: Above onboarding API is to the one running in iLab.

Run as part of the stack:

```
docker run -v genprocessor:/work --link onboarding-api:onboarding-api -d genprocessor-job
```

## http

Build:

```
$ cd http
$ docker build -t genprocessor-http .
```

Run:

```
$ docker run -p 8080:80 -d -v genprocessor:/www/data:ro genprocessor-http
```

