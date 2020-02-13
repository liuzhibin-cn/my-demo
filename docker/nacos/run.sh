#!/bin/sh

docker run -d --net=mydemo --name nacos -p 18848:8848 -e MYSQL_HOST=mysql mydemo/nacos