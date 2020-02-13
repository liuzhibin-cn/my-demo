#!/bin/sh

docker run -d --net=mydemo --name item -e NACOS_HOST=nacos mydemo/item