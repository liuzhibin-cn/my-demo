FROM mydemo/skywalking-base:6.6.0 AS build

WORKDIR /home/skywalking
RUN rm -rf oap-libs webapp

FROM mydemo/openjdk:8-jre-alpine

COPY --from=build /home/skywalking /home/skywalking
ENV SKYWALKING_HOST=skywalking SKYWALKING_PORT=12800