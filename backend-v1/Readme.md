# Backend-v1

This sub-project will serve as the new backend instead of the monolith.
It is meant to be called from [tm-ui-v2](../tm-ui-v2/). 

## Build

```
mvn clean install -P mysql fabric8:build -D docker.image.name=jetzlstorfer/ticket-monster-backend-v1:latest
```

## Exchange Dockerfile
Due to some permission issues the Dockerfile in ```backend-v1\target\docker\<USER>\ticket-monster-backend-v1\latest\build\``` has to be edited to this content:
```
FROM jboss/wildfly:10.1.0.Final 
EXPOSE 8080

COPY maven $JBOSS_HOME/

USER root

#Give correct permissions when used in an OpenShift environment.
RUN chown -R jboss:0 $JBOSS_HOME/ && \
    chmod -R g+rw $JBOSS_HOME/

USER jboss
```

Build new image and push to Dockerhub
```
# from backend-v1\target\docker\jetzlstorfer\ticket-monster-backend-v1\latest\build directory
docker build . -t jetzlstorfer/ticket-monster-backend-v1:latest

docker push jetzlstorfer/ticket-monster-backend-v1:latest
```

## Create application in OpenShift

```
oc new-app --docker-image=jetzlstorfer/ticket-monster-backend-v1:latest
```

## Set environment variables for database connection

Since the backend has to communicate with the mysql database, we have to set the according environment variables for the deployment description.

```
oc env dc/ticket-monster-backend-v1 MYSQL_SERVICE_HOST=[IP from the DB service] MYSQL_SERVICE_PORT=3306
```
check if environment variables are set
```
oc env dc/ticket-monster-backend-v1 --list
#or
oc env pods --all --list
```