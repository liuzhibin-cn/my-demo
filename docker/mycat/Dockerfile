FROM mydemo/openjdk:8-jre-alpine

# Download Mycat
RUN curl -fL -o /home/mycat.tar.gz http://dl.mycat.org.cn/1.6.7.3/20190927161129/Mycat-server-1.6.7.3-release-20190927161129-linux.tar.gz \
	&& tar xzf /home/mycat.tar.gz -C /home/ \
	&& rm -rf /home/mycat.tar.gz
WORKDIR /home/mycat
ADD ./scripts/ ./
# sh files with Windows format can't run in container (Alpine Linux), transform to unix format
RUN dos2unix bin/docker-entrypoint.sh && dos2unix bin/startup_nowrap.sh

ENV MYSQL_HOST=mysql MYSQL_PORT=3306 MYSQL_USER=mydemo MYSQL_PSW=mydemo
EXPOSE 8066 9066
ENTRYPOINT ["bash", "bin/docker-entrypoint.sh"]
