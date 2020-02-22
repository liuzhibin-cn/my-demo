#!/bin/sh

docker run -d --net=mydemo --name stock -e SERVICE_HOST=stock -e NACOS_HOST=nacos -e ZIPKIN_HOST=zipkin -e SKYWALKING_HOST=skywalking-oap mydemo/stock