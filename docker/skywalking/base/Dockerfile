FROM mydemo/openjdk:8-jre-alpine AS build

WORKDIR /home
# Download SkyWalking
RUN curl -fL -o ./skywalking.tar.gz http://mirror.bit.edu.cn/apache/skywalking/6.6.0/apache-skywalking-apm-6.6.0.tar.gz \
	&& tar xzf skywalking.tar.gz -C ./ \
	&& rm -rf skywalking.tar.gz \
	&& mv apache-skywalking-apm-bin skywalking \
	&& curl -fL -o ./skywalking/oap-libs/mysql-connector-java-8.0.18.jar http://maven.aliyun.com/nexus/content/groups/public/mysql/mysql-connector-java/8.0.18/mysql-connector-java-8.0.18.jar \
	&& rm -rf ./skywalking/bin
ADD ./configs/ /home/skywalking/
# sh files with Windows format can't run in container (Alpine Linux), transform to unix format
RUN dos2unix /home/skywalking/oap.sh && dos2unix /home/skywalking/ui.sh