FROM java:8

# java:8镜像：
# OS: Debian 8.7 [cat /etc/issue.net, cat /etc/debian_version]
# java: openjdk version "1.8.0_111"
# git: 2.1.4

WORKDIR /home
ADD ./dist/ /home/nacos/
ENV MYSQL_HOST=mysql MYSQL_USER=nacos MYSQL_PSW=nacos

# nacos的bin/startup.sh启动脚本为Windows环境创建，在java:8镜像中执行报错，需要将文件格式转换为unix格式
RUN wget -nv http://ftp.br.debian.org/debian/pool/main/d/dos2unix/dos2unix_6.0.4-1_amd64.deb \
	&& dpkg -i dos2unix_6.0.4-1_amd64.deb \
	&& cd nacos/bin \
	&& dos2unix startup.sh
EXPOSE $NACOS_PORT
ENTRYPOINT ["/bin/bash", "/home/nacos/bin/startup.sh", "-m", "standalone"]