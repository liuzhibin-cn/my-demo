#!/bin/sh

cd `dirname "$0"`

echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
echo ">>> Build ShardingProxy image"
docker build -t mydemo/shardingproxy:4.0.0-RC3 .
echo "<<< Finished: mydemo/shardingproxy:4.0.0-RC3"