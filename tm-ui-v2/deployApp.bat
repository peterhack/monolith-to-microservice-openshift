cd C:\dynatrace\repos\johannes-b\monolith\tm-ui-v2
docker build -t jbraeuer/tm-ui:backend-2 .
docker push jbraeuer/tm-ui:backend-2
cf push tm-ui-v3 -o jbraeuer/tm-ui:backend-2