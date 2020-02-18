FROM mydemo/openjdk:8-jre-alpine

# Download ShardingProxy
RUN curl -fL -o /home/shardingproxy.tar.gz http://mirrors.tuna.tsinghua.edu.cn/apache/incubator/shardingsphere/4.0.0-RC3/apache-shardingsphere-incubating-4.0.0-RC3-sharding-proxy-bin.tar.gz \
	&& tar xzf /home/shardingproxy.tar.gz -C /home/ \
	&& rm -rf /home/shardingproxy.tar.gz \
	&& mv /home/apache-shardingsphere-incubating-4.0.0-RC3-sharding-proxy-bin /home/shardingproxy \
	&& curl -fL -o /home/shardingproxy/lib/mysql-connector-java-5.1.47.jar http://maven.aliyun.com/nexus/content/groups/public/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47.jar
WORKDIR /home/shardingproxy
ADD ./scripts/ ./
# sh files with Windows format can't run in container (Alpine Linux), transform to unix format
RUN dos2unix bin/docker-entrypoint.sh

ENV MYSQL_HOST=mysql MYSQL_PORT=3306 MYSQL_USER=mydemo MYSQL_PSW=mydemo
EXPOSE 3307
ENTRYPOINT ["bin/docker-entrypoint.sh"]