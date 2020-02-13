#!/bin/sh

DIR=`dirname "$0"`
cd $DIR

docker build -t mydemo/user .