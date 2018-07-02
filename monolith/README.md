# TicketMonster Monolith

## Build Ticketmonster

Using Mavon on Windows, the Ticketmonster Monolith can be built from scratch, make sure you enter your own Docker name:
```
mvn clean install -P mysql fabric8:build -D docker.image.name=<DOCKERHUB>/ticket-monster-mysql:latest
```

<!--
## Run Ticketmonster locally

```
TODO
docker run -d -p 
```
-->

## Push image to Dockerhub

```
docker push <DOCKERHUB>/ticket-monster-mysql:latest
```


## Create project in Openshift

exchange Dockerfile in ```monolith/target/docker/jetzlstorfer/ticket-monster-mysql/latest/build``` with this content:
```
FROM jboss/wildfly:10.1.0.Final
EXPOSE 8080
COPY maven /opt/jboss/wildfly/

USER root 
RUN chmod -R 777 ${JBOSS_HOME}
USER jboss
```

Build the Docker image and push to dockerhub
```
docker build . -t jetzlstorfer/ticket-monster-mysql:latest
docker push jetzlstorfer/ticket-monster-mysql:latest
```


create project, setup mysql service and create monolith app

```
oc new-project m2m
oc new-app -e MYSQL_USER=ticket -e MYSQL_PASSWORD=monster -e MYSQL_DATABASE=ticketmonster mysql:5.5 

oc new-app --docker-image=jetzlstorfer/ticket-monster-mysql:latest
oc expose service ticket-monster-mysql --name monolith
```

## Monolith-Proxy

Next step, deploy the [proxy](../monolith-proxy/) in front of the monolith.

## Apply the OneAgent operator to your OpenShift cluster

Don't forget to apply the Dynatrace OneAgent Operator on your OpenShift cluster.
Find the [instructions here](../dynatrace-oneagent-operator/).

