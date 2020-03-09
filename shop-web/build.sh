#!/bin/sh

# Dockerfile Maven: https://github.com/spotify/dockerfile-maven
# Use shell script to build Docker image because additional process is required in case of SkyWalking enabled

# The value will be set by package.sh
APM=zipkin

DIR=`dirname "$0"`
cd $DIR

if [ "$APM" = "skywalking" ]; then
    cp Dockerfile.skywalking Dockerfile
else
    cp Dockerfile.default Dockerfile
fi

docker build -t mydemo/shopweb .
rm -rf Dockerfile