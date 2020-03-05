FROM mydemo/openjdk:8-jre-alpine

# Official download: https://github.com/alibaba/nacos/releases
# Download from github-mirror.bugkiller.org instead of github.com to improve speed, to resolve network issues in Chine.
# ATTENTION: Security risks not clear!
RUN curl -fL -o /home/nacos.tar.gz http://github-mirror.bugkiller.org/alibaba/nacos/releases/download/1.1.4/nacos-server-1.1.4.tar.gz \
	&& tar xzf /home/nacos.tar.gz -C /home/ \
	&& rm -rf /home/nacos.tar.gz
WORKDIR /home/nacos
ADD ./scripts/ ./
# sh files with Windows format can't run in container (Alpine Linux), transform to unix format
RUN dos2unix ./init.sh && dos2unix ./bin/startup.sh && ./init.sh

ENV MYSQL_HOST=mysql MYSQL_PORT=3306 MYSQL_DB=nacos MYSQL_USER=nacos MYSQL_PSW=nacos
EXPOSE 8848
ENTRYPOINT ["bash", "bin/startup.sh", "-m", "standalone"]