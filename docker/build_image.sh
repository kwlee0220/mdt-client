#! /bin/bash

docker image rmi kwlee0220/mdt-client

cp ../build/libs/mdt-client-0.9.1-all.jar mdt-client-all.jar

docker build -t kwlee0220/mdt-client:latest .
docker push kwlee0220/mdt-client:latest
