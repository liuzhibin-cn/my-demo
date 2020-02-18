#!/bin/sh

# package.sh根据打包参数设置该环境变量值
MYSQL_HOST=shardingproxy
docker run -d --net=mydemo --name user -e MYSQL_HOST=$MYSQL_HOST -e NACOS_HOST=nacos -e ZIPKIN_HOST=zipkin -e SKYWALKING_HOST=skywalking-oap mydemo/user