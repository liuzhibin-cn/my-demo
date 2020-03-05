FROM openjdk:8-jre-alpine

# Change Alpine Linux apk source to mirror site in China.
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.ustc.edu.cn/g' /etc/apk/repositories && apk add --no-cache bash dos2unix curl
