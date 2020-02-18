#!/bin/bash

cd `dirname "$0"`

echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
echo ">>> Build Nacos image"
echo ">>> Official download: https://github.com/alibaba/nacos/releases"
echo ">>> Download from github-mirror.bugkiller.org instead of github.com to improve speed."
echo ">>> ATTENTION: Security risks not clear!"
docker build -t mydemo/nacos:1.1.4 .
echo "<<< Finished: mydemo/nacos:1.1.4"