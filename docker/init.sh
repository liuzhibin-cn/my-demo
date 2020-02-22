#!/bin/sh

cd `dirname "$0"`
BASE_DIR=`pwd`

set -e

docker network create mydemo

./mysql/build.sh
./mysql/run.sh

./java/build.sh

./mycat/build.sh
./mycat/run.sh

#./shardingproxy/build.sh
#./shardingproxy/run.sh

./nacos/build.sh
./nacos/run.sh

#./seata/build.sh
#./seata/run.sh

#./skywalking/build.sh
#./skywalking/run.sh

./zipkin/build.sh
./zipkin/run.sh
