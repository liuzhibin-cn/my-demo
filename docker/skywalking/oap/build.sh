#!/bin/sh

cd `dirname "$0"`
docker build --rm -t mydemo/skywalking-oap:6.6.0 .