FROM mydemo/skywalking-base:6.6.0 AS build

WORKDIR /home/skywalking
RUN rm -rf agent webapp

FROM mydemo/openjdk:8-jre-alpine

COPY --from=build /home/skywalking /home/skywalking
WORKDIR /home/skywalking
ENV MYSQL_HOST=mysql MYSQL_PORT=3306 MYSQL_USER=skywalking MYSQL_PSW=skywalking MYSQL_DB=skywalking
EXPOSE 12800 11800
ENTRYPOINT ["./oap.sh"]