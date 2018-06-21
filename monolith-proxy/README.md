# Monolith proxy

needed for real user monitoring

## Deploy

```
docker build . -t <YOURDOCKER>/monolith-proxy:latest
docker push <YOURDOCKER>/monolith-proxy:latest
oc new-app <YOURDOCKER>/monolith-proxy
```