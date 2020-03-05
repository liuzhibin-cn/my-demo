#!/bin/bash

SHARDINGPROXY_HOME=`dirname "$0"`
cd $SHARDINGPROXY_HOME/..
SHARDINGPROXY_HOME=`pwd`

if [[ -z "$MYSQL_HOST" || -z "$MYSQL_PORT" || -z "$MYSQL_USER" || -z "$MYSQL_PSW" ]]; then
    echo "ENV: MYSQL_HOST, MYSQL_PORT, MYSQL_USER or MYSQL_PSW is empty"
    exit 1
fi
sed -i "s/jdbc:mysql:\/\/[^\/]*/jdbc:mysql:\/\/$MYSQL_HOST:$MYSQL_PORT/g" $SHARDINGPROXY_HOME/conf/config-order.yaml
sed -i "s/username:.*/username: $MYSQL_USER/g" $SHARDINGPROXY_HOME/conf/config-order.yaml
sed -i "s/password:.*/password: $MYSQL_PSW/g" $SHARDINGPROXY_HOME/conf/config-order.yaml
sed -i "s/jdbc:mysql:\/\/[^\/]*/jdbc:mysql:\/\/$MYSQL_HOST:$MYSQL_PORT/g" $SHARDINGPROXY_HOME/conf/config-user.yaml
sed -i "s/username:.*/username: $MYSQL_USER/g" $SHARDINGPROXY_HOME/conf/config-user.yaml
sed -i "s/password:.*/password: $MYSQL_PSW/g" $SHARDINGPROXY_HOME/conf/config-user.yaml

# 启动ShardingProxy
CLASS_PATH=.:${SHARDINGPROXY_HOME}/conf:${SHARDINGPROXY_HOME}/lib/*
JAVA_OPTS=" -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true "
JAVA_MEM_OPTS=""
MAIN_CLASS=org.apache.shardingsphere.shardingproxy.Bootstrap
if [ $# == 1 ]; then
    MAIN_CLASS=${MAIN_CLASS}" "$1
    echo "The port is configured as $1"
fi
if [ $# == 2 ]; then
    MAIN_CLASS=${MAIN_CLASS}" "$1" "$2
    echo "The port is configured as $1"
    echo "The configuration file is $SHARDINGPROXY_HOME/conf/$2"
fi
java ${JAVA_OPTS} ${JAVA_MEM_OPTS} -classpath ${CLASS_PATH} ${MAIN_CLASS}