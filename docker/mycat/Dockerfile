FROM openjdk:8

# Debian 10.2 [cat /etc/issue.net, cat /etc/debian_version]
# openjdk version "1.8.0_242"
# OpenJDK Runtime Environment (build 1.8.0_242-b08)
# OpenJDK 64-Bit Server VM (build 25.242-b08, mixed mode)
# git: 2.20.1

WORKDIR /home
# 下载mycat
RUN wget -P ./ http://dl.mycat.io/1.6.7.3/20190927161129/Mycat-server-1.6.7.3-release-20190927161129-linux.tar.gz \
	&& tar xzf Mycat-server-1.6.7.3-release-20190927161129-linux.tar.gz -C ./ \
	&& rm -rf Mycat-server-1.6.7.3-release-20190927161129-linux.tar.gz
# 配置mycat
ADD ./conf/* /home/mycat/conf/
ADD ./bin/* /home/mycat/bin/
# bin/start_from_docker.sh为Windows格式，在容器中执行报错，需将文件格式转换为unix格式
ADD ./dos2unix_6.0.4-1_amd64.deb /home/dos2unix_6.0.4-1_amd64.deb
RUN dpkg -i dos2unix_6.0.4-1_amd64.deb && dos2unix /home/mycat/bin/start_from_docker.sh

ENV MYSQL_HOST=mysql MYSQL_PORT=3306 MYSQL_USER=mydemo MYSQL_PSW=mydemo
EXPOSE 8066 9066
ENTRYPOINT ["/bin/bash", "/home/mycat/bin/start_from_docker.sh"]