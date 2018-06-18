cd C:\dynatrace\repos\johannes-b\monolith\orders-service\
mvn clean install -P mysql,kubernetes -D skipITs=true fabric8:build
cd .\target\docker\jbraeuer\orders-service\latest\build\
docker build -t jbraeuer/orders-service:latest .
docker push jbraeuer/orders-service:latest
cf push orders-service -o jbraeuer/orders-service:latest
