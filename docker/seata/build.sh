#!/bin/bash

DIR=`dirname "$0"`
cd $DIR
./nacos-config.sh -h localhost -p 18848

docker build -t mydemo/seata .