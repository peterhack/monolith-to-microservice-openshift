# Dynatrace OneAgent Operator

This is the home of Dynatrace OneAgent Operator which supports the rollout and lifecycle of [Dynatrace OneAgent](https://www.dynatrace.com/support/help/get-started/introduction/what-is-oneagent/) in Kubernetes and OpenShift clusters.
Rolling out Dynatrace OneAgent via DaemonSet on a cluster is straightforward.
Maintaining its lifecycle places a burden on the operational team.
Dynatrace OneAgent Operator closes this gap by automating the repetitive steps involved in keeping Dynatrace OneAgent at its latest desired version.

## Installation
Please refer to the official repository hosted here https://github.com/Dynatrace/dynatrace-oneagent-operator for detailed installation instructions.

Alternatively, you can run the ```apply.sh``` script that automates the installation of the OneAgent on your cluster. Please edit the ```$masterurl``` according to your installation.
