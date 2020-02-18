FROM mydemo/openjdk:8-jre-alpine

WORKDIR /home
ADD ./zipkin.jar /home/

ENV STORAGE_TYPE=mysql MYSQL_HOST=mysql MYSQL_TCP_PORT=3306 MYSQL_DB=zipkin MYSQL_USER=zipkin MYSQL_PASS=zipkin 
EXPOSE 9411
ENTRYPOINT ["java", "-jar", "/home/zipkin.jar"]