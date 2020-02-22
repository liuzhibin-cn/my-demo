#!/bin/sh

cd `dirname "$0"`
BASE_DIR=`pwd`

set -e

kubectl create -f deployment/mysql-deployment.yaml
sleep 3
kubectl create -f deployment/mycat-deployment.yaml
kubectl create -f deployment/nacos-deployment.yaml
kubectl create -f deployment/zipkin-deployment.yaml