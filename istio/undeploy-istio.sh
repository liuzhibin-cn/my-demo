#!/bin/sh

kubectl delete Deployment web-shop-v1 web-shop-v2 svc-order svc-user svc-stock svc-item
kubectl delete StatefulSet pub-zipkin pub-nacos mycat-demo db-demo
kubectl delete Service web-shop pub-zipkin pub-nacos mycat-demo db-demo
kubectl delete VirtualService web-shop
kubectl delete DestinationRule web-shop
kubectl delete Gateway web-shop-gateway -n istio-system