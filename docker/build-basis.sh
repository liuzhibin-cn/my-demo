#!/bin/sh

cd `dirname "$0"`
BASE_DIR=`pwd`

set -e

./mysql/build.sh

./java/build.sh

./mycat/build.sh

./shardingproxy/build.sh

./nacos/build.sh

./seata/build.sh

./skywalking/build.sh

./zipkin/build.sh
