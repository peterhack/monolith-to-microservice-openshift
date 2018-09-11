# Monolith proxy

Simple http proxy that sits in front of the monolith and redirects the traffic from and to the monolith.

<!--
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
-->

## Deploy

```
oc get routes
oc new-app -e MONOLITHURL=<yourmonolithurl> --docker-image=jetzlstorfer/monolith-proxy:latest
```

## Expose it to the public

We want to create a public accessible route for the monolith which we call "production". 
```
oc expose svc/monolith-proxy --name=production
```