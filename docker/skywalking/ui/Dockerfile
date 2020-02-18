FROM mydemo/skywalking-base:6.6.0 AS build

WORKDIR /home/skywalking
RUN rm -rf agent oap-libs

FROM mydemo/openjdk:8-jre-alpine

COPY --from=build /home/skywalking /home/skywalking
WORKDIR /home/skywalking
ENV SKYWALKING_HOST=skywalking SKYWALKING_PORT=12800
EXPOSE 8080
ENTRYPOINT ["./ui.sh"]