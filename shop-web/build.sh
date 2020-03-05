#!/bin/sh

# The value will be set by package.sh
APM=zipkin

DIR=`dirname "$0"`
cd $DIR

if [ "$APM" = "skywalking" ]; then
    cp Dockerfile.skywalking Dockerfile
else
    cp Dockerfile.normal Dockerfile
fi

docker build -t mydemo/shopweb .
rm -rf Dockerfile