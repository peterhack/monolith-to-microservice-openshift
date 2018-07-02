#!/usr/bin/env bash
mvn clean install -P mysql fabric8:build -D docker.image.name=jetzlstorfer/ticket-monster-mysql:latest