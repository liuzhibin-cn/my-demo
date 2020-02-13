#!/bin/sh

docker run -d --net=mydemo --name shopweb -p18090:8090 -e NACOS_HOST=nacos mydemo/shopweb