#!/bin/sh

cd `dirname "$0"`

echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
echo ">>> Build Mycat image"
docker build -t mydemo/mycat:1.6.7.3 .
echo "<<< Finished: mydemo/mycat:1.6.7.3"