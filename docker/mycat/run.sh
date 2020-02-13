#!/bin/sh

docker run -d --net=mydemo --name mycat -p 18066:8066 -p 19066:9066 -e MYSQL_HOST=mysql mydemo/mycat