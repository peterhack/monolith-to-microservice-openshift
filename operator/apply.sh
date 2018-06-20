#/bin/bash!

oc login $masterurl -u system:admin
oc create -f namespace.yaml
oc adm policy add-scc-to-user privileged -z default -n dynatrace
oc annotate project dynatrace openshift.io/node-selector=""
oc create -f rbac.yaml
oc create -f crd.yaml
oc create -f operator.yaml
oc create -f cr.yaml

oc get deployment -n dynatrace

sleep 15

watch oc get pods -n dynatrace
