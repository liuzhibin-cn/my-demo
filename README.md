-------------------------------------------------------------------
#### 架构
![](docs/images/architecture.png) <br />
`ItemService`、`StockService`没有建表，应用中生成mock数据。

-------------------------------------------------------------------
#### 运行演示项目
1. JDK8+，配置、部署Redis、MySQL、Mycat、SkyWalking；
2. MySQL建表，参考[sql-schema.sql](docs/sql-schema.sql)；
3. 运行演示项目：
   1. 先本地编译安装`service-client`：`mvn install`；
   2. 依次启动`item-service`、`stock-service`、`user-service`、`order-service`，最后启动`test-app`执行演示用例；

运行SpringBoot项目方法：
```sh
# 1. 直接用maven运行
mvn spring-boot:run
# 2. 打包运行
mvn clean package spring-boot:repackage
java -jar xxx-service-0.0.1-SNAPSHOT.jar.jar
```

-------------------------------------------------------------------
#### 数据库水平拆分
参考[]()

-------------------------------------------------------------------
#### 全链路跟踪APM
- PinPoint：
  1. 将项目中所有`logback.xml`使用`logback-pinpoint.xml`内容替换；
  2. 部署和使用方法参考[PinPoint演示](docs/APM-PinPoint.md)；
- SkyWalking：
  1. 打开parent `pom.xml`中注释掉的SkyWalking依赖项；
  2. 打开service-item项目my.demo.utils.Tracer中注释掉的代码；
  3. 打开test-app项目my.demo.test.Application.runTestCaseWithTrace方法上注释掉的Trace注解；
  4. 将项目中所有`logback.xml`使用`logback-skywalking.xml`内容替换；
  5. 部署和使用方法参考[SkyWalking演示](docs/APM-SkyWalking.md)；
- ZipKin：参考[ZipKin演示](docs/APM-ZipKin.md)；

-------------------------------------------------------------------
#### Dubbo基础用法
使用[apache/dubbo-spring-boot-project](https://github.com/apache/dubbo-spring-boot-project)与SpringBoot集成，注册中心使用Redis。

1. `pom.xml`添加依赖项：
   ```xml
   <dependency>
        <groupId>org.apache.dubbo</groupId>
        <artifactId>dubbo-spring-boot-starter</artifactId>
        <version>2.7.4.1</version>
   </dependency>
   <dependency>
     <groupId>org.apache.dubbo</groupId>
     <artifactId>dubbo</artifactId>
     <version>2.7.4.1</version>
   </dependency>
   <dependency> <!-- dubbo: serialization -->
     <groupId>de.ruedigermoeller</groupId>
     <artifactId>fst</artifactId>
     <version>2.57</version>
   </dependency>
   <dependency> <!-- dubbo: use redis registry, dubbo uses jedis client -->
       <groupId>redis.clients</groupId>
       <artifactId>jedis</artifactId>
   </dependency>
   ```
2. 在`application.yml`中配置protocol、registry等：
   ```yaml
   dubbo:
       application:
           id: srv-item
           name: srv-item
           qosEnable: false
        protocol:
            id: dubbo
            name: dubbo
            port: 20880
            threads: 3
            iothreads: 1
            server: netty
            client: netty
            status: server
            serialization: fst
            queues: 0
            keepAlive: true
        registry: 
            id: redis
            address: redis://127.0.0.1:6379
   ```
   最新配置项参考[ApplicationConfig](https://github.com/apache/dubbo/blob/master/dubbo-common/src/main/java/org/apache/dubbo/config/ApplicationConfig.java)、[ProtocolConfig](https://github.com/apache/dubbo/blob/master/dubbo-common/src/main/java/org/apache/dubbo/config/ProtocolConfig.java)、[RegistryConfig](https://github.com/apache/dubbo/blob/master/dubbo-common/src/main/java/org/apache/dubbo/config/RegistryConfig.java)、[MonitorConfig](https://github.com/apache/dubbo/blob/master/dubbo-common/src/main/java/org/apache/dubbo/config/MonitorConfig.java)、[ServiceConfig](https://github.com/apache/dubbo/blob/master/dubbo-config/dubbo-config-api/src/main/java/org/apache/dubbo/config/ServiceConfig.java)、[ReferenceConfig](https://github.com/apache/dubbo/blob/master/dubbo-config/dubbo-config-api/src/main/java/org/apache/dubbo/config/ReferenceConfig.java)
3. SpringBoot启动类上指定Dubbo组件扫描范围：
   ```java
   @Configuration
   @EnableAutoConfiguration
   @ComponentScan(basePackages={"my.demo.service.item"})
   @DubboComponentScan(basePackages = { "my.demo.service.item" })
   public class Application {
	   public static void main(String[] args) {
		   new SpringApplicationBuilder(Application.class)
			   .web(WebApplicationType.NONE).run(args);
	   }
   }
   ```
4. 暴露Dubbo服务的类上使用`@Service`注解（不再需要Spring的`@Component`），引用Dubbo服务使用`@Reference`（不再需要Spring的`@Autowired`）；
