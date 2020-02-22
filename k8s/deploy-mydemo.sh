#!/bin/sh

cd `dirname "$0"`
BASE_DIR=`pwd`

set -e

kubectl create -f deployment/svc-item-deployment.yaml
kubectl create -f deployment/svc-stock-deployment.yaml
echo "Waiting 4 seconds for item and stock pods to start"
sleep 4
kubectl create -f deployment/svc-user-deployment.yaml
kubectl create -f deployment/svc-order-deployment.yaml
echo "Waiting 4 seconds for user and order pods to start"
sleep 4
kubectl create -f deployment/web-shopweb-deployment.yaml