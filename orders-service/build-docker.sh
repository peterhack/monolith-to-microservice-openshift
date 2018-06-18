#!/usr/bin/env bash
mvn clean install -P mysql,kubernetes -D docker.image.name=jbraeuer/orders-service:latest,skipITs=true fabric8:build
