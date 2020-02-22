#!/bin/sh

# package.sh根据打包参数设置该环境变量值
MYSQL_HOST=mycat
docker run -d --net=mydemo --name order -e SERVICE_HOST=order -e MYSQL_HOST=$MYSQL_HOST -e NACOS_HOST=nacos -e ZIPKIN_HOST=zipkin -e SKYWALKING_HOST=skywalking-oap mydemo/order