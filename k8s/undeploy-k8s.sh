#!/bin/sh

kubectl delete deployment web-shopweb
kubectl delete deployment svc-order
kubectl delete deployment svc-user
kubectl delete deployment svc-stock
kubectl delete deployment svc-item
kubectl delete statefulset pub-zipkin
kubectl delete statefulset pub-nacos
kubectl delete statefulset mycat-mydemo
kubectl delete statefulset db-mydemo

kubectl delete service web-shopweb
kubectl delete service pub-zipkin
kubectl delete service pub-nacos
kubectl delete service mycat-mydemo
kubectl delete service db-mydemo