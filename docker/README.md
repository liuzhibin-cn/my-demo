#### Comments on building docker images
1. For java applications, `openjdk:8-jre-alpine` is a better choice for base images.
   - `openjdk:8`: OS is Debian 10.2, image size: **510MB**;
   - `openjdk:8-jre-alpine`: OS is Alpine Linux, image size: **84.9MB**;
2. Use `curl` instead of `wget` to download to avoid progress status issues. 
   - `curl`: <br />
     ![](../docs/images/docker-download-curl.png)
   - `wget`: <br />
     ![](../docs/images/docker-download-wget.png)
3. In some cases [multi-stage builds](https://docs.docker.com/develop/develop-images/multistage-build/) is a very
   convenient way to build images for 3-party/complicated applications, see [docker/skywalking](skywalking/) for an example.
