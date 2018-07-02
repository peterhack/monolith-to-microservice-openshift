# Dynatrace OneAgent Operator

This is the home of Dynatrace OneAgent Operator which supports the rollout and lifecycle of [Dynatrace OneAgent](https://www.dynatrace.com/support/help/get-started/introduction/what-is-oneagent/) in Kubernetes and OpenShift clusters.
Rolling out Dynatrace OneAgent via DaemonSet on a cluster is straightforward.
Maintaining its lifecycle places a burden on the operational team.
Dynatrace OneAgent Operator closes this gap by automating the repetitive steps involved in keeping Dynatrace OneAgent at its latest desired version.

## Installation
Please refer to the official repository hosted here https://github.com/Dynatrace/dynatrace-oneagent-operator for detailed installation instructions.



Edit the ```cr.yaml``` file and enter your tenant ID, API token, as well as the PaaS token. Simply execute ```operator/apply.sh```
which executes the following steps:
```
oc login YOUROPENSHIFT-MASTERURL -u system:admin
oc create -f namespace.yaml
oc adm policy add-scc-to-user privileged -z default -n dynatrace
oc annotate project dynatrace openshift.io/node-selector=""
oc create -f rbac.yaml
oc create -f crd.yaml
oc create -f operator.yaml
oc create -f cr.yaml

oc get deployment -n dynatrace
```


Alternatively, you can run the ```apply.sh``` script that automates the installation of the OneAgent on your cluster. Please edit the ```$masterurl``` according to your installation.
