#!/bin/sh

docker run -d --net=mydemo --name skywalking-oap -e MYSQL_HOST=mysql -e MYSQL_PORT=3306 -e MYSQL_USER=skywalking -e MYSQL_PSW=skywalking -e MYSQL_DB=skywalking mydemo/skywalking-oap:6.6.0