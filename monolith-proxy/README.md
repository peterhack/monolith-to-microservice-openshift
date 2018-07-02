# Monolith proxy

Simple http proxy that sits in front of the monolith and redirects the traffic from and to the monolith.

## Configuration
Make sure you put your URL of the monolith in place in the ```httpd.conf``` file:
```
ProxyPass "/" "http://monolith-m2m.YOURURL/"
ProxyPassReverse "/" "http://monolith-m2m.YOURURL/"
```

## Deploy

```
docker build . -t <YOURDOCKER>/monolith-proxy:latest
docker push <YOURDOCKER>/monolith-proxy:latest

# if needed, also create a new application in OpenShift
oc new-app <YOURDOCKER>/monolith-proxy
```