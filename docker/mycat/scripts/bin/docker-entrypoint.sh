#!/bin/sh

MYCAT_HOME=`dirname "$0"`
cd $MYCAT_HOME/..
MYCAT_HOME=`pwd`

if [[ -z "$MYSQL_HOST" || -z "$MYSQL_PORT" || -z "$MYSQL_USER" || -z "$MYSQL_PSW" ]]; then
    echo "ENV: MYSQL_HOST, MYSQL_PORT, MYSQL_USER or MYSQL_PSW is empty"
    exit 1
fi

sed -i "s/host=\"[^\"]*\"/host=\"$MYSQL_HOST\"/g" $MYCAT_HOME/conf/schema.xml
sed -i "s/url=\"[^\"]*\"/url=\"$MYSQL_HOST:$MYSQL_PORT\"/g" $MYCAT_HOME/conf/schema.xml
sed -i "s/user=\"[^\"]*\"/user=\"$MYSQL_USER\"/g" $MYCAT_HOME/conf/schema.xml
sed -i "s/password=\"[^\"]*\"/password=\"$MYSQL_PSW\"/g" $MYCAT_HOME/conf/schema.xml

$MYCAT_HOME/bin/startup_nowrap.sh