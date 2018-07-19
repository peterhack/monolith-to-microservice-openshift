in standalone.xml set database credentials


mvn clean install -P mysql fabric8:build -D docker.image.name=jetzlstorfer/backend-v2:latest

exchange dockerfile

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

docker build
docker push

oc new-app
oc expose


FF4J console
http://backend-v2-m2m.18.207.174.41.xip.io/ff4j-console
 



oc import-image backend-v2:latest --from=jetzlstorfer/backend-v2:latest