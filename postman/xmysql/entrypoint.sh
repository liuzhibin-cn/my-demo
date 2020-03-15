#!/bin/sh

# If -r is not specified, xmysql will listen on 127.0.0.1, it's not accessable outside the container.
xmysql -h $MYSQL_HOST -o $MYSQL_PORT -u $MYSQL_USER -p $MYSQL_PSW -d $DB -r 0.0.0.0