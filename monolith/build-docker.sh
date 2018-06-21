#!/usr/bin/env bash
mvn clean install -Pmysql fabric8:build -Ddocker.image.name=jetzlstorfer/ticket-monster-mysql:latest