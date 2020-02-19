#!/bin/bash

CLASSPATH="config:$CLASSPATH"
for i in oap-libs/*.jar
do
    CLASSPATH="$i:$CLASSPATH"
done

echo "Location: `pwd`"
SW_JDBC_URL=jdbc:mysql://$MYSQL_HOST:$MYSQL_PORT/$MYSQL_DB
SW_DATA_SOURCE_USER=$MYSQL_USER
SW_DATA_SOURCE_PASSWOR=$MYSQL_PSW
echo "SW_JDBC_URL: $SW_JDBC_URL"
echo "SW_DATA_SOURCE_USER: $SW_DATA_SOURCE_USER"

java ${JAVA_OPTS} -classpath ${CLASSPATH} org.apache.skywalking.oap.server.starter.OAPServerStartUp "$@"