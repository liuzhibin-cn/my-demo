[English](README-en.md) | [中文](README.md)

A simple demo application for building scalable applications using **Microservices**, **Database Sharding** Proxies, **Flexible Transactions**, **APM** tools, and deploying to **Docker** and **Kubernetes**.

-------------------------------------------------------------------
## Demo Application Architecture
![](docs/images/architecture.png)

- [Dubbo](http://dubbo.apache.org/en-us/): A high-performance, java based open source RPC framework. \
  `Dubbo` employs a client based, decentralized load balance mechanism. Consumers fetch providers from registry servers to client at startup, create long living TCP connections and comunicate directly to providers (for `dubbo/thrift` protocols), with variant configurable load balance and fail over algorithms. Availability of providers is detected by heartbeat, and new provider registration events are notified by registry servers to all consumers. \
  `Dubbo` provides better performance than `Spring Cloud`, it can be deployed and scaled in `Kubernetes`, using its own service discovery, load balance and fail over mechanisms, but it's difficult to work with `Istio`.
- [Nacos](https://github.com/alibaba/nacos): A naming and config service, providing more enhancements on service discovery and flow control than [Zookeeper](http://zookeeper.apache.org/).
- [ShardingProxy](http://shardingsphere.apache.org/), [Mycat](https://github.com/MyCATApache/Mycat-Server): Both are database sharding proxies, providing a transparent database sharding solution. \
  The internals are very similar, both implement `MySQL` protocol to comunicate with cross platform applications, intercept SQL queries and route to backend `MySQL` servers based on sharding keys and configurable sharding rules. Although it's not recommemded but both of them support cross-sharding queries (without sharding keys in SQL), rewrite SQL if necessary, dispatch queries to all backend `MySQL` servers, gather results and do aggregation, sorting, pagination in proxy memory, and return result to client.
- [Seata](https://github.com/seata/seata): A flexible transaction framework for distributed applications. \
  `Seata` implements three transaction modes: [AT](https://seata.io/en-us/docs/dev/mode/at-mode.html), [TCC](https://seata.io/en-us/docs/dev/mode/tcc-mode.html) and [SAGA](https://seata.io/en-us/docs/dev/mode/saga-mode.html). The demo application uses `AT` mode, it's transparent for application code.
- [ZipKin](https://zipkin.io/), [PinPoint](https://github.com/naver/pinpoint), [SkyWalking](https://skywalking.apache.org/): APM tools for microservices, `ZipKin` and `SkyWalking` can work with `Istio`.

-------------------------------------------------------------------
## Run demo application

### Prerequisites
1. OS: Linux, Mac, or Windows with a bash shell, such as git bash;
   > In Mac OSX `gnu-sed` is required: `brew install gnu-sed`
2. JDK 8+ and apache maven;
3. `Docker`, 6GB memory for `Docker Desktop` is recommended;

### Package demo application
Use [package.sh](package.sh) to compile and package the demo application. 

Usage: 
1. Options to enable database sharding: `-mycat`, `-shardingproxy`
2. Options to enable global transaction management: `-seata`
3. Options to enable APM: `-skywalking`, `-pinpoint`, `-zipkin`

Example:
```sh
package.sh -mycat -zipkin
package.sh -shardingproxy -pinpoint -seata
```

### Run in local machine
1. `MySQL` and `Nacos` must be installed, see [MySQL scripts](docker/mysql/scripts) and [Nacos quickstart](https://nacos.io/en-us/docs/quick-start.html). 
2. Install and start 3-party components as you wanted, go to project home for help, and [Dockerfiles and scripts](docker/) in demo application are also references for you.
3. Edit parent [pom.xml](https://github.com/liuzhibin-cn/my-demo/blob/master/pom.xml) and config for your local environment.
4. Use `package.sh` to compile and package demo application.
5. Start demo application as following steps: \
   - Neither `SkyWalking` nor `PinPoint` is enabled:
     ```sh
     java -jar item-service\target\item-service-0.0.1-SNAPSHOT.jar
     java -jar stock-service\target\stock-service-0.0.1-SNAPSHOT.jar
     java -jar user-service\target\user-service-0.0.1-SNAPSHOT.jar
     java -jar order-service\target\order-service-0.0.1-SNAPSHOT.jar
     java -jar shop-web\target\shop-web-0.0.1-SNAPSHOT.jar
     ```
   - `SkyWalking` is enabled:
     ```sh
     java -javaagent:F:\sw\agent\skywalking-agent.jar -Dskywalking.agent.service_name=svc-item -jar item-service\target\item-service-0.0.1-SNAPSHOT.jar
     java -javaagent:F:\sw\agent\skywalking-agent.jar -Dskywalking.agent.service_name=svc-stock -jar stock-service\target\stock-service-0.0.1-SNAPSHOT.jar
     java -javaagent:F:\sw\agent\skywalking-agent.jar -Dskywalking.agent.service_name=svc-user -jar user-service\target\user-service-0.0.1-SNAPSHOT.jar
     java -javaagent:F:\sw\agent\skywalking-agent.jar -Dskywalking.agent.service_name=svc-order -jar order-service\target\order-service-0.0.1-SNAPSHOT.jar
     java -javaagent:F:\sw\agent\skywalking-agent.jar -Dskywalking.agent.service_name=shop-web -jar shop-web\target\shop-web-0.0.1-SNAPSHOT.jar
	 ```
   - `PinPoint` is enabled:
     ```sh
     java -javaagent:F:\pinpoint\agent\pinpoint-bootstrap-1.8.5.jar -Dpinpoint.agentId=svc-item-1 -Dpinpoint.applicationName=svc-item -jar item-service\target\item-service-0.0.1-SNAPSHOT.jar
     java -javaagent:F:\pinpoint\agent\pinpoint-bootstrap-1.8.5.jar -Dpinpoint.agentId=svc-stock-1 -Dpinpoint.applicationName=svc-stock -jar stock-service\target\stock-service-0.0.1-SNAPSHOT.jar
     java -javaagent:F:\pinpoint\agent\pinpoint-bootstrap-1.8.5.jar -Dpinpoint.agentId=svc-user-1 -Dpinpoint.applicationName=svc-user -jar user-service\target\user-service-0.0.1-SNAPSHOT.jar
     java -javaagent:F:\pinpoint\agent\pinpoint-bootstrap-1.8.5.jar -Dpinpoint.agentId=svc-order-1 -Dpinpoint.applicationName=svc-order -jar order-service\target\order-service-0.0.1-SNAPSHOT.jar
     java -javaagent:F:\pinpoint\agent\pinpoint-bootstrap-1.8.5.jar -Dpinpoint.agentId=shop-web-1 -Dpinpoint.applicationName=shop-web -jar shop-web\target\shop-web-0.0.1-SNAPSHOT.jar
	 ```

Entrypoints:
- Demo Application: [localhost:8090/shop](http://localhost:8090/shop)
- `Nacos`: [localhost:8848/nacos](http://localhost:8848/nacos), user/password: `nacos/nacos`
- `ZipKin`: [localhost:9411/zipkin](http://localhost:9411/zipkin/)
- `SkyWalking`: [localhost:8080](http://localhost:8080/)
- `PinPoint`: `localhost:{your-port}`
- `Mycat`: data port `8066`, management port is `9066`, use `mysql` to connect, user/password: `mydemo/mydemo`
  > In Mac OSX, `--protocol` must be specified: 
  > ```sh
  > mysql -h localhost -P 8066 -umydemo -pmydemo --protocol=TCP
  > ```
- `ShardingProxy`: `3307`, use `mysql` to connect, user/password: `mydemo/mydemo`

### Run in Docker
1. Build Docker images for all 3-party components used in demo application.
   > ATTENTION: Docker support for `PinPoint` is not provided in demo application, `-pinpoint` option cann't be used in `package.sh` if you run the demo application in Docker.
   ```sh
   docker/build-basis.sh
   ```
2. Run Docker containers for all 3-party components. It's recommended for you that take `docker/deploy-basis.sh` as a reference, 
and only run those containers you wanted, to avoid Docker hungs because of memory pressure.
   ```sh 
   docker/deploy-basis.sh
   ```
   All containers run in a `Docker Network` `mydemo` created in `docker/deploy-basis.sh`, `docker-compose` is not used.
3. Use [docker/deploy-mydemo.sh](docker/deploy-mydemo.sh) to build images and run containers for demo application. \
   Usage:
   - `-build`: Build images for `item`, `stock`, `user`, `order` services and `shop-web` app.
   - `-run`: Run containers for `item`, `stock`, `user`, `order` services and `shop-web` app.
   - `-stop`: Stop containers for `item`, `stock`, `user`, `order` services and `shop-web` app.
   - `-rm`: Remove containers for `item`, `stock`, `user`, `order` services and `shop-web` app.
   - `-rmi`: Remove images for `item`, `stock`, `user`, `order` services and `shop-web` app.

Example:
```sh
docker/build-basis.sh
docker/deploy-basis.sh
# Run demo application with Mycat, Seata and ZipKin
package.sh -mycat -seata -zipkin
docker/deploy-mydemo.sh -build -run
# Run demo application with ShardingProxy and SkyWalking
package.sh -shardingproxy -skywaling
docker/deploy-mydemo.sh -stop -rm -rmi -build -run
```

Entrypoints:
- Demo Application: [localhost:18090/shop](http://localhost:18090/shop)
- `Nacos`: [localhost:18848/nacos](http://localhost:18848/nacos), user/password: `nacos/nacos`
- `ZipKin`: [localhost:19411/zipkin](http://localhost:19411/zipkin/)
- `SkyWalking`: [localhost:18080](http://localhost:18080/)
- `Mycat`: data port `18066`, management port `19066`, use `mysql` to connect, user/password: `mydemo/mydemo`
  > In Mac OSX, `--protocol` must be specified: 
  > ```sh
  > mysql -h localhost -P 18066 -umydemo -pmydemo --protocol=TCP
  > ```
- `ShardingProxy`: `13307`, use `mysql` to connect, user/password: `mydemo/mydemo`
- `MySQL`：`13306`, use `mysql` to connect, user/password: `root/123`

![](docs/images/docker-containers.png)

![](docs/images/docker-stats.png)

### Run in Kubernetes
The YAML and script files in [k8s/](k8s/) run the demo application with `Mycat` and `ZipKin` in `Kubernetes`.

1. Build Docker images for all 3-party components used in the demo application.
   ```sh
   docker/build-basis.sh
   ```
2. Deploy demo application in `Kubernetes`:
   ```sh
   k8s/deploy-k8s.sh
   ```

Entrypoints:
- Demo Application: [localhost:30090/shop](http://localhost:30090/shop)
- `Nacos`: [localhost:30048/nacos](http://localhost:30048/nacos), user/password: `nacos/nacos`
- `ZipKin`: [localhost:30041/zipkin](http://localhost:30041/zipkin/)
- `Mycat`: data port is `30066`, management port is `30067`, use `mysql` to connect, user/password: `mydemo/mydemo`
  > In Mac OSX, `--protocol` must be specified: 
  > ```sh
  > mysql -h localhost -P 30066 -umydemo -pmydemo --protocol=TCP
  > ```
- `MySQL`：`30006`, use `mysql` to connect, user/password: `root/123`

**Dubbo in Kubernetes** \
`Dubbo` services are deployed by `Kubernetes` `Deployment`, and not registered as `Kubernetes` `Service`. They use Dubbo's own service descovery, load balance, providers take `POD IP` to register to `Nacos`, consumers fetch providers from `Nacos` and comunicate with all providers by `POD IP`. `Dubbo` services can be managed by `Deployment`, the following example scripts show how to scale `svc-user` to 3 PODs. New POD ready and existing POD terminated events can be discovered by `Dubbo`. 
```sh
# Scale user-service to 3 PODs
kubectl scale Deployment svc-user --replicas=3
# Open 3 terminals to watch user-service logs
# 1. Find user-service PODs
kubectl get pods | grep svc-user
# 2. Watch logs for each svc-user POD
kubectl logs svc-user-68ff844499-9zqf8 -c svc-user -f
kubectl logs svc-user-68ff844499-dgsnx -c svc-user -f
...
# 3. Open http://localhost:30090/shop, Click "Run a Full TestCase" button and watch which user-service instance is used.
```

![](docs/images/kubernetes-overview.png)

### Run with Istio
The YAML and script files in [istio/](istio/) is a simple example for running `shop-web` with `Istio`, YAML files are the same with those in [k8s/](k8s/) except `web-shop-deployment.yaml`. It's tested in `Docker Desktop`, with `Istio` installed and `default` namespace enabled for `istio-injection` as following:
```sh
istioctl manifest apply --set profile=demo
kubectl label ns default istio-injection=enabled --overwrite
```

Run demo application:
```sh
docker/build-basis.sh
istio/deploy-istio.sh
```

Bind `myshop.com` to local machine IP in `hosts` file, and use [http://myshop.com/hello/YourName](http://myshop.com/hello/YourName) to visit.

`shop-web` is deployed with two versions: 2 PODs for `v1` and 1 POD for `v2`, default route to `v1`, use URL query param `version=v2` to route to `v2`. This is achieved by `VirtualService` and `DestinationRule` in [istio/deployment/web-shop-deployment.yaml](istio/deployment/web-shop-deployment.yaml).

`Dubbo` services using `dubbo/thrift/rmi` protocols cann't work with `Istio`, if use `http/rest/webservice` protocols, `Spring Boot` is a better choice rather than `Dubbo`.

-------------------------------------------------------------------
## Screenshots
Logs for `order-service`, debug logs for `Seata` is printed: \
![](docs/images/order-service-out.png)

Data distribution in shards: \
![](docs/images/db-sharding-1.png) \
![](docs/images/db-sharding-2.png)

`Mycat` explain: \
![](docs/images/mycat-explain.png)

`ZipKin`: \
![](https://richie-leo.github.io/ydres/img/10/120/1013/screen-trace-detail-sql.png)

`PinPoint`: \
![](https://richie-leo.github.io/ydres/img/10/120/1012/pinpoint-screen-trace-mixed-view.png)

`FitNesse`：\
![](https://richie-leo.github.io/ydres/img/10/191/1001/fitnesse-test.png)

`Postman`: \
![](https://richie-leo.github.io/ydres/img/10/191/1000/postman.jpg)

`Newman`: \
![](https://richie-leo.github.io/ydres/img/10/191/1000/newman-output.jpg)
