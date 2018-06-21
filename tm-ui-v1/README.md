## Ticket Monster UI-V1

The purpose of this sub-project is to deploy it next to the ```monolith/``` project to show how this UI can directly communicate with the monolith.



## Start this project
Use the image provided by this project
```
oc new-app --docker-image=jetzlstorfer/ticket-monster-ui-v1:latest
```
or build the project by yourself and push it to docker
```
docker build . -t <YOURDOCKER>/ticket-monster-ui-v1:latest
docker push <YOURDOCKER>/ticket-monster-ui-v1:latest
oc new-app --docker-image=<YOURDOCKER>/ticket-monster-ui-v1:latest
```

## Make it publicly available
Expose the service in OpenShift
```
oc expose service ticket-monster-ui-v1 --name ui-v1
```


This proxy helps us keep friendly URLs even when there are composite UIs or composite microservice REST apis
It also helps us avoid tripping the browser Same Origin policy. We use a simple HTTP server (apache) to serve the static content and then use the reverse proxy plugins to proxy REST calls to the appropriate microservice:

```
# proxy for the admin microserivce
ProxyPass "/rest" "http://ticket-monster:8080/rest"
ProxyPassReverse "/rest" "http://ticket-monster:8080/rest"
```
## Configuration

In ```httpd.conf``` the redirect to the monolith was inserted as a proxy:

```
ProxyPass "/rest" "http://<YOURURL>/rest"
ProxyPassReverse "/rest" "http://<YOURURL>/rest"
```

In contrast to the [original project](https://github.com/ticket-monster-msa/monolith) the base image in the ```Dockerfile``` was changed from httpd:2.4 to centos/httpd-24-centos7 in order to work in OpenShift smoothly.

