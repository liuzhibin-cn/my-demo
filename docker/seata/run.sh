#!/bin/sh

docker run -d --net=mydemo --name seata -e MYSQL_HOST=mysql mydemo/seata