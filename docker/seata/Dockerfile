FROM mydemo/openjdk:8-jre-alpine

# Official download: https://github.com/seata/seata/releases
# Download from github-mirror.bugkiller.org instead of github.com to improve speed.
# ATTENTION: Security risks not clear!
RUN curl -fL -o /home/seata.tar.gz http://github-mirror.bugkiller.org/seata/seata/releases/download/v1.0.0/seata-server-1.0.0.tar.gz \
	&& tar xzf /home/seata.tar.gz -C /home/ \
	&& rm -rf /home/seata.tar.gz
WORKDIR /home/seata
ADD ./scripts/ ./
# sh files with Windows format can't run in container (Alpine Linux), transform to unix format
RUN dos2unix bin/seata-server.sh \
	&& dos2unix conf/init.sh \
	&& dos2unix conf/nacos-config.sh

ENV NACOS_HOST=nacos NACOS_PORT=8848 MYSQL_HOST=mysql MYSQL_PORT=3306 MYSQL_DB=seata MYSQL_USER=seata MYSQL_PSW=seata SEATA_HOST=seata SEATA_PORT=8091
EXPOSE 8091
ENTRYPOINT ["bin/seata-server.sh", "-h", "127.0.0.1", "-p", "$SEATA_PORT", "-m", "db", "-n", "1", "-e", "test"]