# OrderService a Microservice for TicketMonster



## Instructions

```
oc new-app mysql:5.5 --name=orders-mysql -e MYSQL_USER=ticket -e MYSQL_PASSWORD=monster -e MYSQL_DATABASE=orders

$ mvn clean install -P mysql,kubernetes fabric8:build -D docker.image.name=jetzlstorfer/orders-service:latest -D skipTests


# \target\docker\<your dockerhub account>\orders-service\latest\build\
docker build . -t jetzlstorfer/orders-service:latest

docker push jetzlstorfer/orders-service:latest

oc new-app --docker-image=jetzlstorfer/orders-service:latest


```


## set up database

```
# copy files to db pod
oc rsync . <your-db-pod>:/var/lib/mysql

# connect to db pod
oc rsh <your-db-pod>
mysql -u root orders < V1__0_ordersdb-schema.sql
mysql -u root orders < V1__1_ordersdb-data.sql



```
