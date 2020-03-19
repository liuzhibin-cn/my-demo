#!/bin/sh

cd `dirname $0`
mvn clean package -P dev
docker stop fitnesse
docker rm fitnesse
docker rmi mydemo/fitnesse:latest
docker build -t mydemo/fitnesse .