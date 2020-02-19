#!/bin/sh

docker run -d --net=mydemo --name shopweb -p18090:8090 -e NACOS_HOST=nacos -e ZIPKIN_HOST=zipkin -e SKYWALKING_HOST=skywalking-oap mydemo/shopweb