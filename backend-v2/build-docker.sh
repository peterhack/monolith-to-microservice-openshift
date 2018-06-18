#!/usr/bin/env bash
mvn clean install -Pmysql fabric8:build -Ddocker.image.name=jbraeuer/backend:v2

