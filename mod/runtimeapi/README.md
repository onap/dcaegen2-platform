# Runtime API

HTTP API to support runtime environment for DCAE-MOD. It has two major functionalities:
1)  accepts changes on the flow-graph via fbp protocol
2) generate and distribute blueprints based on the change made on the flow-graph


## Prerequisite

If the docker image is not built then

- Build current project
```
mvn clean install

```

- Dockerize the  web module of the current project
```
cd runtime-web
docker build -t runtime-api:latest .
```

## Run Docker container
Note that DASHBOARD related variables should be set in the container
```
docker run --name runtime-api -d -p 9090:9090 \
 -e DASHBOARD_URL=<url> -e DASHBOARD_USERNAME=<username> -e DASHBOARD_PASSWORD=<password> \
 runtime-api:latest
```


docker run --name runtime-api -d -p 9090:9090 \
 -e DASHBOARD_URL=https://dcae-inventory/dcae-service-types \
 runtime-api:latest
