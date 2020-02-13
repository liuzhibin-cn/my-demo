FROM openjdk:8

# Debian 10.2 [cat /etc/issue.net, cat /etc/debian_version]
# openjdk version "1.8.0_242"
# OpenJDK Runtime Environment (build 1.8.0_242-b08)
# OpenJDK 64-Bit Server VM (build 25.242-b08, mixed mode)
# git: 2.20.1

WORKDIR /home
RUN wget -P ./ http://github-mirror.bugkiller.org/seata/seata/releases/download/v1.0.0/seata-server-1.0.0.tar.gz \
	&& tar xzf seata-server-1.0.0.tar.gz -C ./ \
	&& rm -rf seata-server-1.0.0.tar.gz
COPY ./registry.conf /home/seata/conf/
COPY ./seata-server.sh /home/seata/bin/
# seata-server.sh为Windows格式，在容器中执行报错，需将文件格式转换为unix格式
ADD ./dos2unix_6.0.4-1_amd64.deb /home/dos2unix_6.0.4-1_amd64.deb
RUN dpkg -i dos2unix_6.0.4-1_amd64.deb && dos2unix /home/seata/bin/seata-server.sh

EXPOSE 8091
ENTRYPOINT ["/home/seata/bin/seata-server.sh", "-h", "127.0.0.1", "-p", "8091", "-m", "db", "-n", "1", "-e", "test"]