#!/bin/sh

# The value will be set by package.sh
MYSQL_HOST=mysql
docker run -d --net=mydemo --name order -e SERVICE_HOST=order -e MYSQL_HOST=$MYSQL_HOST -e NACOS_HOST=nacos -e ZIPKIN_HOST=zipkin -e SKYWALKING_HOST=skywalking-oap mydemo/order