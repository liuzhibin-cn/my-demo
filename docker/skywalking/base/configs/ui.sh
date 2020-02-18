#!/bin/bash

pwd
echo "java ${JAVA_OPTS} -jar webapp/skywalking-webapp.jar --spring.config.location=webapp/webapp.yml"
java ${JAVA_OPTS} -jar webapp/skywalking-webapp.jar --spring.config.location=webapp/webapp.yml "$@"