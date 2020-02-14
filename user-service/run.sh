#!/bin/sh

MYSQL_HOST=mycat
docker run -d --net=mydemo --name user -e MYSQL_HOST=$MYSQL_HOST -e NACOS_HOST=nacos -e ZIPKIN_HOST=zipkin -e SKYWALKING_HOST=skywalking mydemo/user