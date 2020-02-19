#!/bin/sh

cd `dirname "$0"`

echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
echo ">>> Build an openjdk image as the parent image for those run a java application."
echo ">>> It download openjdk from Docker Hub, and install some shell packages (VERY SLOW)."
echo ">>> Please wait ... "
docker build -t mydemo/openjdk:8-jre-alpine .
echo "<<< Finished: mydemo/openjdk:8-jre-alpine"