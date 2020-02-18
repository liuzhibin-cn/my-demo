#!/bin/sh

cd `dirname "$0"`
docker build -t mydemo/skywalking-base:6.6.0 .