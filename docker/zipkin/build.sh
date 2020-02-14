#!/bin/sh

# 要求本地安装以下依赖项：
# 1. docker client；
# 2. git client；

DIR=`dirname "$0"`
cd $DIR

# zipkin官方下载方法：curl -sSL https://zipkin.io/quickstart.sh | bash -s
# 这里对quickstart.sh进行了一些修改：
# 1. 从阿里云maven镜像仓库下载，加快速度；
# 2. quickstart.sh默认下载最新版本，这里指定使用2.19.2版本；
./quickstart.sh

docker build -t mydemo/zipkin:2.19.2 .