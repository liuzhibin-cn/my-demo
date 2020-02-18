#!/bin/sh

cd `dirname "$0"`

echo ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
echo ">>> Build SkyWalking images:"
echo ">>> 1. skywalking-base: A full SkyWalking distribution package, including OAP service,"
echo ">>>    webapp (UI) and agent, ONLY \"AS build\"."
echo ">>> 2. skywalking-oap: OAP service. The webapp and agent were removed."
echo ">>> 3. skywalking-ui: The webapp. OAP service and agent were removed."
echo ">>> 4. skywalking-client: A parent image for SkyWalking clients, only including agent,"
echo ">>>    OAP service and webapp were removed."
./base/build.sh
echo "<<< Finished: mydemo/skywalking-base:6.6.0"
./oap/build.sh
echo "<<< Finished: mydemo/skywalking-oap:6.6.0"
./ui/build.sh
echo "<<< Finished: mydemo/skywalking-ui:6.6.0"
./client/build.sh
echo "<<< Finished: mydemo/skywalking-client:6.6.0"
