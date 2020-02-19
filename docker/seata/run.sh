#!/bin/sh

docker run -d --net=mydemo --name seata -e NACOS_HOST=nacos -e NACOS_PORT=8848 -e MYSQL_HOST=mysql -e MYSQL_PORT=3306 -e MYSQL_USER=seata -e MYSQL_PSW=seata -e SEATA_HOST=seata -e SEATA_PORT=8091 mydemo/seata:1.0.0