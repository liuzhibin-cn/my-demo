#!/bin/sh

docker run -d --net=mydemo --name skywalking-ui -e SKYWALKING_HOST=skywalking-oap -p 18080:8080 mydemo/skywalking-ui:6.6.0