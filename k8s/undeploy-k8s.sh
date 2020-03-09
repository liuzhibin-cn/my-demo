#!/bin/sh

kubectl delete Deployment web-shop
kubectl delete Deployment svc-order
kubectl delete Deployment svc-user
kubectl delete Deployment svc-stock
kubectl delete Deployment svc-item
kubectl delete StatefulSet pub-zipkin
kubectl delete StatefulSet pub-nacos
kubectl delete StatefulSet mycat-demo
kubectl delete StatefulSet db-demo

kubectl delete Service web-shop
kubectl delete Service pub-zipkin
kubectl delete Service pub-nacos
kubectl delete Service mycat-demo
kubectl delete Service db-demo