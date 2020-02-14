#!/bin/sh

# 将seata配置导入nacos，必须先启动nacos容器
NACOS_START=`docker ps -f name=nacos | grep nacos -c`
if [ "$NACOS_START" != "1" ]; then
    echo "Please start nacos container first!"
	exit 1
else
	echo "Nacos container is running, try to import seata configuration to nacos."
fi
DIR=`dirname "$0"`
cd $DIR
./nacos-config.sh -h localhost -p 18848

docker run -d --net=mydemo --name seata -e MYSQL_HOST=mysql mydemo/seata:1.0.0