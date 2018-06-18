cd C:\dynatrace\repos\johannes-b\monolith\backend-v1
mvn clean install -P mysql fabric8:build -D docker.image.name=jbraeuer/backend:v1
cd .\target\docker\jbraeuer\backend\v1\build\
docker build -t jbraeuer/backend:v1 .
docker push jbraeuer/backend:v1
cf push backend-v1 -o jbraeuer/backend:v1