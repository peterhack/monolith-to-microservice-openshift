#!/bin/bash

oc login $masterurl -u system:admin

oc delete -f cr.yaml
oc delete -f operator.yaml
oc delete -f crd.yaml
oc delete -f rbac.yaml
oc delete -f namespace.yaml

