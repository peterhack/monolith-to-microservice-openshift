#!/usr/bin/env bash
mvn clean install -Pmysql fabric8:build -Ddocker.image.name=jbraeuer/ticket-monster-mysql:latest