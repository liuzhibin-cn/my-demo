#!/bin/sh

MYSQL_HOST=mycat
docker run -d --net=mydemo --name order -e MYSQL_HOST=$MYSQL_HOST -e NACOS_HOST=nacos mydemo/order