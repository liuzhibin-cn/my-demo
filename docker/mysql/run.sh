#!/bin/sh

docker run -d --net=mydemo --name mysql -p 13306:3306 -e MYSQL_ROOT_PASSWORD=123 mydemo/mysql:5.7.18