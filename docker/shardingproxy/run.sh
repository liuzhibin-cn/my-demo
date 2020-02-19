#!/bin/sh

docker run -d --net=mydemo --name shardingproxy -p 13307:3307 -e MYSQL_HOST=mysql mydemo/shardingproxy:4.0.0-RC3