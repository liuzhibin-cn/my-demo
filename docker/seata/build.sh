#!/bin/bash

cd `dirname "$0"`

echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
echo ">>> Build Seata image"
echo ">>> Official download: https://github.com/seata/seata/releases"
echo ">>> Download from github-mirror.bugkiller.org instead of github.com to improve speed."
echo ">>> ATTENTION: Security risks not clear!"
docker build -t mydemo/seata:1.0.0 .
echo "<<< Finished: mydemo/seata:1.0.0"