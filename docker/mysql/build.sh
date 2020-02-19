#!/bin/bash

cd `dirname "$0"`

echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
echo ">>> Build MySQL image, all schemas required by this demo application will be initialized."
docker build -t mydemo/mysql:5.7.18 .
echo "<<< Finished: mydemo/mysql:5.7.18"