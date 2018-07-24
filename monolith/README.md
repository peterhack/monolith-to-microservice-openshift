# TicketMonster Monolith

## Build Ticketmonster

Using Mavon on Windows, the Ticketmonster Monolith can be built from scratch, make sure you enter your own Docker name:
```
mvn clean install -P mysql fabric8:build -D docker.image.name=<DOCKERHUB>/ticket-monster-monolith:latest
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
docker push <DOCKERHUB>/ticket-monster-monolith:latest
```


## Create project in Openshift

exchange Dockerfile in ```target/docker/jetzlstorfer/ticket-monster-monolith/latest/build``` with this content:
```dockerfile
FROM jboss/wildfly:10.1.0.Final 
EXPOSE 8080

COPY maven $JBOSS_HOME/

USER root

#Give correct permissions when used in an OpenShift environment.
RUN chown -R jboss:0 $JBOSS_HOME/ && \
    chmod -R g+rw $JBOSS_HOME/

USER jboss

```

Build the Docker image and push to dockerhub
```
docker build . -t jetzlstorfer/ticket-monster-monolith:latest
docker push jetzlstorfer/ticket-monster-monolith:latest
```


create project, setup mysql service and create monolith app

```
oc new-project m2m
oc new-app -e MYSQL_USER=ticket -e MYSQL_PASSWORD=monster -e MYSQL_DATABASE=ticketmonster mysql:5.5 

oc new-app -e MYSQL_SERVICE_HOST=your-mysql-host -e MYSQL_SERVICEP_PORT=3306 --docker-image=jetzlstorfer/ticket-monster-monolith:latest
oc expose service ticket-monster-monolith --name monolith
```

### Check the route

```
oc get routes 
```
will get you something like
```
NAME       HOST/PORT                                 PATH      SERVICES                 PORT       TERMINATION   WILDCARD
monolith   monolith-workshop1.YOURIP.xip.io                    ticket-monster-monolith  8080-tcp                 None
```

## Monolith-Proxy

Next step, deploy the [proxy](../monolith-proxy/) in front of the monolith.

## Apply the OneAgent operator to your OpenShift cluster

Don't forget to apply the Dynatrace OneAgent Operator on your OpenShift cluster.
Find the [instructions here](../dynatrace-oneagent-operator/).

