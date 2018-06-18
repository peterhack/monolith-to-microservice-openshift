cd C:\dynatrace\repos\johannes-b\monolith\tm-ui-v1
docker build -t jbraeuer/tm-ui:monolith .
docker push jbraeuer/tm-ui:monolith
cf push tm-ui-v1 -o jbraeuer/tm-ui:monolith