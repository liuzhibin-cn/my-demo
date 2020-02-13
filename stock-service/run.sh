#!/bin/sh

docker run -d --net=mydemo --name stock -e NACOS_HOST=nacos mydemo/stock