#!/bin/sh

# package.sh根据打包参数设置该环境变量值
APM=zipkin

DIR=`dirname "$0"`
cd $DIR

if [ "$APM" = "skywalking" ]; then
    cp Dockerfile.skywalking Dockerfile
else
    cp Dockerfile.normal Dockerfile
fi

docker build -t mydemo/stock .
rm -rf Dockerfile