FROM mydemo/openjdk:8-jre-alpine

#RUN curl -fL -o /home/fitnesse-standalone.jar http://fitnesse.org/fitnesse-standalone.jar?responder=releaseDownload&release=20200308
ADD fitnesse-standalone.jar /home/
ADD target/lib/ /home/lib
ADD target/fitnesse-0.0.1-SNAPSHOT.jar /home/lib/

ENV NACOS_HOST=nacos MYSQL_HOST=mysql
EXPOSE 8011
ENTRYPOINT ["java", "-jar", "/home/fitnesse-standalone.jar", "-p", "8011", "-l", "/home/fitnesse.log"]