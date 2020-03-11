#!/bin/sh

kubectl delete Deployment web-shop svc-order svc-user svc-stock svc-item
kubectl delete StatefulSet pub-zipkin pub-nacos mycat-demo db-demo
kubectl delete Service web-shop pub-zipkin pub-nacos mycat-demo db-demo