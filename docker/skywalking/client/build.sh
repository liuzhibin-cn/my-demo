#!/bin/sh

cd `dirname "$0"`
docker build -t mydemo/skywalking-client:6.6.0 .