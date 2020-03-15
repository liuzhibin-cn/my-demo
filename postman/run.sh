#!/bin/bash

cd `dirname $0`
HOME=`pwd`

docker run -d --net=mydemo --name xmysql mydemo/xmysql:0.4.9

NEWMAN_HOME="/Users/richie-home/Documents/workspace/my-demo/postman/newman"
docker run --net=mydemo --name newman -v "$NEWMAN_HOME:/etc/newman" -it mydemo/newman:4.6.0 newman run mydemo.postman_collection.json -e docker-env.postman_environment.json --reporters cli,json --reporter-json-export mydemo-report.json

docker stop xmysql
docker rm xmysql newman
