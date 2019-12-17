#!/usr/bin/env sh

echo "> Run mvn clean package spring-boot:repackage for all services and shop-app application:"
echo ">    ./package.sh"
echo "> To enable ZipKin, SkyWalking or PinPoint APM tools, use one of the following parameters:"
echo ">    ./package.sh [ zipkin | skywalking | pinpoint ]"

if [[ ! -z "$1" ]] && [[ "$1" != "zipkin" ]] && [[ "$1" != "skywalking" ]] && [[ "$1" != "pinpoint" ]]; then
	echo "> Invalid parameter: $1, expected values: zipkin, skywalking, pinpoint"
	exit 1
fi;

PROJECT_HOME=`dirname "$0"`
cd $PROJECT_HOME
mvn install
cd service-client
mvn clean install
cd ..

cd item-service
if [ -z "$1" ]; then
	mvn clean package spring-boot:repackage
else
	mvn clean package spring-boot:repackage -Pdev,"$1"
fi;
cd ..

cd stock-service
if [ -z "$1" ]; then
	mvn clean package spring-boot:repackage
else
	mvn clean package spring-boot:repackage -Pdev,"$1"
fi;
cd ..

cd user-service
if [ -z "$1" ]; then
	mvn clean package spring-boot:repackage
else
	mvn clean package spring-boot:repackage -Pdev,"$1"
fi;
cd ..

cd order-service
if [ -z "$1" ]; then
	mvn clean package spring-boot:repackage
else
	mvn clean package spring-boot:repackage -Pdev,"$1"
fi;
cd ..

cd shop-web
if [ -z "$1" ]; then
	mvn clean package spring-boot:repackage
else
	mvn clean package spring-boot:repackage -Pdev,"$1"
fi;

echo "> Finished"