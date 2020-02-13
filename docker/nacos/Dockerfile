FROM openjdk:8

# Debian 10.2 [cat /etc/issue.net, cat /etc/debian_version]
# openjdk version "1.8.0_242"
# OpenJDK Runtime Environment (build 1.8.0_242-b08)
# OpenJDK 64-Bit Server VM (build 25.242-b08, mixed mode)
# git: 2.20.1

WORKDIR /home
# dist目录由build.sh脚本本地源码编译nacos生成
ADD ./dist/ /home/nacos/
# nacos的bin/startup.sh为Windows格式，在容器中执行报错，需将文件格式转换为unix格式
ADD ./dos2unix_6.0.4-1_amd64.deb /home/dos2unix_6.0.4-1_amd64.deb
RUN dpkg -i dos2unix_6.0.4-1_amd64.deb && dos2unix /home/nacos/bin/startup.sh

ENV MYSQL_HOST=mysql MYSQL_USER=nacos MYSQL_PSW=nacos
EXPOSE 8848
ENTRYPOINT ["/bin/bash", "/home/nacos/bin/startup.sh", "-m", "standalone"]