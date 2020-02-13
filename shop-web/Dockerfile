FROM openjdk:8

# Debian 10.2 [cat /etc/issue.net, cat /etc/debian_version]
# openjdk version "1.8.0_242"
# OpenJDK Runtime Environment (build 1.8.0_242-b08)
# OpenJDK 64-Bit Server VM (build 25.242-b08, mixed mode)
# git: 2.20.1

WORKDIR /home
ENV NACOS_HOST=nacos WEB_PORT=8090
ADD ./target/shop-web-0.0.1-SNAPSHOT.jar /home/
EXPOSE $WEB_PORT
ENTRYPOINT ["java", "-jar", "/home/shop-web-0.0.1-SNAPSHOT.jar"]