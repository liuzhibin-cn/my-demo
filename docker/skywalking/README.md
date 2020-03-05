Build 4 SkyWalking images:
1. `skywalking-base`: A full SkyWalking distribution package, including OAP service, webapp (UI) and agent. <br />
   It's the base image for other 3 SkyWalking images using [multi-stage builds](https://docs.docker.com/develop/develop-images/multistage-build/).
2. `skywalking-oap`: SkyWalking OAP service. The webapp and agent were removed.
3. `skywalking-ui`: SkyWalking webapp (UI). OAP service and agent were removed.
4. `skywalking-client`: A parent image for SkyWalking clients, with agent reserved, OAP service and webapp were removed.