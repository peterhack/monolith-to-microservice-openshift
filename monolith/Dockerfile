FROM jboss/wildfly:10.1.0.Final 
EXPOSE 8080

COPY maven $JBOSS_HOME/standalone/

USER root

#Give correct permissions when used in an OpenShift environment.
RUN chown -R jboss:0 $JBOSS_HOME/standalone && \
    chmod -R g+rw $JBOSS_HOME/standalone

USER jboss
