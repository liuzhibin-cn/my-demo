#!/bin/sh

kubectl delete Deployment web-shop
kubectl delete Deployment svc-order
kubectl delete Deployment svc-user
kubectl delete Deployment svc-stock
kubectl delete Deployment svc-item
kubectl delete StatefulSet pub-zipkin
kubectl delete StatefulSet mycat-demo
kubectl delete StatefulSet db-demo

kubectl delete Service web-shop
kubectl delete Service svc-order
kubectl delete Service svc-user
kubectl delete Service svc-stock
kubectl delete Service svc-item
kubectl delete Service pub-zipkin
kubectl delete Service mycat-demo
kubectl delete Service db-demo

kubectl delete VirtualService svc-item
kubectl delete VirtualService svc-stock
kubectl delete VirtualService svc-user
kubectl delete VirtualService svc-order
kubectl delete VirtualService pub-zipkin

kubectl delete DestinationRule svc-item
kubectl delete DestinationRule svc-stock
kubectl delete DestinationRule svc-user
kubectl delete DestinationRule svc-order
kubectl delete DestinationRule pub-zipkin