#!/bin/bash

cd `dirname "$0"`

# Change startup process from daemon to foreground mode
sed -i '/echo "$JAVA ${JAVA_OPT}"/'d bin/startup.sh
sed -i 's/nohup.*/$JAVA ${JAVA_OPT} nacos.nacos/g' bin/startup.sh
sed -i '/echo "nacos is starting，you can check the/'d bin/startup.sh
# Decrease memory allocation
sed -i 's/-Xms512m -Xmx512m -Xmn256m/-Xms128m -Xmx512m -Xmn32m/g' bin/startup.sh
# Set MySQL configurations, and load configuration value from environment variables
cp conf/application.properties.example conf/application.properties
sed -i 's/db.num=2/db.num=1/g' conf/application.properties
sed -i 's/db.url.0=jdbc:mysql:\/\/11.162.196.16:3306\/nacos_devtest/db.url.0=jdbc:mysql:\/\/${MYSQL_HOST:127.0.0.1}:${MYSQL_PORT:3306}\/${MYSQL_DB:nacos}/g' conf/application.properties
sed -i '/db.url.1/'d conf/application.properties
sed -i 's/db.user=nacos_devtest/db.user=${MYSQL_USER:nacos}/g' conf/application.properties
sed -i 's/db.password=nacos/db.password=${MYSQL_PSW:nacos}/g' conf/application.properties