#!/bin/sh

docker run -d --net=mydemo --name zipkin -p 19411:9411 -e MYSQL_HOST=mysql mydemo/zipkin:2.19.2