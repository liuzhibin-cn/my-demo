FROM openjdk:8-jre-alpine

# 将Alpine Linux apk安装源改为国内镜像
RUN sed -i 's/dl-cdn.alpinelinux.org/mirrors.ustc.edu.cn/g' /etc/apk/repositories && apk add --no-cache bash dos2unix curl
