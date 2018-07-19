## Ticket Monster Backend V2

This `backend` module contains the monolith Ticket Monster service **without** the UI. (Use an external UI to connect to the REST API that this service exposes.) Besides, this backend version implements the functionality to call the external service OrdersService (see project orders-service). This is controlled using a feature flag defined by [FF4J](http://ff4j.org/). 

## Prerequisites

* Requires access to a PCF Cluster
* Make sure you have [Cloud Foundry CLI](https://docs.cloudfoundry.org/cf-cli/install-go-cli.html) installed 
* You need [Maven](https://maven.apache.org/) to build the monolith
* You need [Docker](https://www.docker.com/community-edition) to create a Docker image 
* [Sign In](https://hub.docker.com/) to your DockerHub Account

## How to use Feature Flags for Java (FF4J) 
**1. Add FF4J dependency to pom.xml**
```xml
<dependency>
    <groupId>org.ff4j</groupId>
    <artifactId>ff4j-core</artifactId>
    <version>${ffj4.version}</version>
</dependency>
```

**2. Define feature flag(s) in ./src/main/resources/ff4j.xml**
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<features xmlns="http://ff4j.org/schema"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://ff4j.org/schema http://ff4j.org/schema/ff4j-1.4.0.xsd">
  <feature uid="orders-internal" enable="false"  description="Continue with legacy orders implementation" />
  <feature uid="orders-service" enable="true"  description="Call new orders microservice" />
</features>
```

**3. Add class FF4jFactory.java to package ./src/main/java/org/jboss/examples/ticketmonster/util**
```java
@ApplicationScoped
public class FF4jFactory implements FF4jProvider{

    private static FF4j rc = new FF4j("ff4j.xml");

    @Produces
    public static FF4j ff4j(){
        return rc;
    }

    @Override
    public FF4j getFF4j() {
        return rc;
    }
}
```

**4. (After a re-deployment of backend-v2) See FF4J console**
```
https://backend-v2.apps.pcfeu.dev.dynatracelabs.com/ff4j-console
```

## Instructions

**0. [Clone the repository](https://github.com/dynatrace-innovationlab/monolith-to-microservice-cloudfoundry#instructions) and change directory**
```sh
$ cd backend-v2
```

**1. Make sure to have a `mysql` Cloud Foundry service instance described [here]()**

If you don't have a ticketMonster-mysql service, create one using:
```sh
$ cf create-service p-mysql 100mb ticketMonster-mysql
```

**2. Build the latest version of the backend-v1 as Docker image**
```sh
$ mvn clean install -P mysql fabric8:build -D docker.image.name=<your dockerhub account>/backend:v2
```

**3. Move to Dockerfile and push Docker image to DockerHub**
```sh
$ cd .\target\docker\<your dockerhub account>\backend\v2\build\
$ docker push <your dockerhub account>/backend:v2
```

**4. Push the application to Cloud Foundry by refering to the container image on DockerHub**
```sh
cf push backend-v2 -o <your dockerhub account>/backend:v2
```

**5. Bind the `mysql` service instance to the application**
```sh
$ cf bind-service backend-v2 ticketMonster-mysql
```

**6. Get binding information (jdbcUrl, name and password) and set environment variables: database connection-url, user-name, and password to these values**
```sh
$ cf env backend-v2
$ cf set-env backend-v2 CONNECTION-URL jdbc:mysql://***
$ cf set-env backend-v2 USER-NAME ***
$ cf set-env backend-v2 PASSWORD ***
```

**7. Restage application to ensure your environment variable changes take effect**
```sh
$ cf restage backend-v2
```