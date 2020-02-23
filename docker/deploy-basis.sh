#!/bin/sh

cd `dirname "$0"`
BASE_DIR=`pwd`

set -e

docker network create mydemo

./mysql/run.sh

./mycat/run.sh

./shardingproxy/run.sh

./nacos/run.sh

./seata/run.sh

./skywalking/run.sh

./zipkin/run.sh