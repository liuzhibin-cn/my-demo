#!/bin/bash

cd `dirname $0`
HOME=`pwd`
cd nodejs
docker build -t mydemo/nodejs:10 .

cd ../xmysql
docker build -t mydemo/xmysql:0.4.9 .

cd ../newman
docker build -t mydemo/newman:4.6.0 .