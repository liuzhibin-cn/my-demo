FROM java:8

# java:8 image:
# OS: Debian 8.7 [cat /etc/issue.net, cat /etc/debian_version]
# java: openjdk version "1.8.0_111"
# git: 2.1.4

WORKDIR /home
ENV NACOS_HOST=nacos WEB_PORT=8090
ADD ./target/shop-web-0.0.1-SNAPSHOT.jar /home/
EXPOSE $WEB_PORT
ENTRYPOINT ["java", "-jar", "/home/shop-web-0.0.1-SNAPSHOT.jar"]