# Distributor API

HTTP API to manage distribution targets for DCAE design. Distribution targets are DCAE runtime environments that have been registered and are enabled to accept flow design changes that are to be orchestrated in that environment.

Run docker container:

```
docker run -d -p 5000:80 --name distributor-api \
    distributor-api:latest
```
