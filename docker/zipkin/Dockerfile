FROM openjdk:8

# Debian 10.2 [cat /etc/issue.net, cat /etc/debian_version]
# openjdk version "1.8.0_242"
# OpenJDK Runtime Environment (build 1.8.0_242-b08)
# OpenJDK 64-Bit Server VM (build 25.242-b08, mixed mode)
# git: 2.20.1

WORKDIR /home
ADD ./zipkin.jar /home/

ENV STORAGE_TYPE=mysql MYSQL_HOST=mysql MYSQL_TCP_PORT=3306 MYSQL_DB=zipkin MYSQL_USER=zipkin MYSQL_PASS=zipkin 
EXPOSE 9411
ENTRYPOINT ["java", "-jar", "/home/zipkin.jar"]