#!/bin/sh

cd $(dirname "$0")

echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
echo ">>> Build ZipKin image"
echo ">>> ZipKin's official download: curl -sSL https://zipkin.io/quickstart.sh | bash -s"
echo ">>> I've made some changes:"
echo ">>> 1. Download from maven.aliyun.com to improve speed."
echo ">>> 2. Download 2.19.2 instead of the latest."
./quickstart.sh

docker build -t mydemo/zipkin:2.19.2 .
rm -rf zipkin*
echo "<<< Finished: mydemo/zipkin:2.19.2"