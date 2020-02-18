#!/bin/sh

cd `dirname "$0"`
docker build -t mydemo/skywalking-ui:6.6.0 .