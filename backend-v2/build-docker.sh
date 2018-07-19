#!/usr/bin/env bash
mvn clean install -Pmysql fabric8:build -Ddocker.image.name=jetzlstorfer/backend-v2:latest

