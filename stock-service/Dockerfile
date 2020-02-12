FROM java:8

# java:8镜像：
# OS: Debian 8.7 [cat /etc/issue.net, cat /etc/debian_version]
# java: openjdk version "1.8.0_111"
# git: 2.1.4

WORKDIR /home
ENV MYSQL_HOST=mysql NACOS_HOST=nacos STOCK_SRV_PORT=20880
ADD ./target/stock-service-0.0.1-SNAPSHOT.jar /home/
EXPOSE $STOCK_SRV_PORT
ENTRYPOINT ["java", "-jar", "/home/stock-service-0.0.1-SNAPSHOT.jar"]