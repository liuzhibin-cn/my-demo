#### 基本用法
使用[apache/dubbo-spring-boot-project](https://github.com/apache/dubbo-spring-boot-project)与`SpringBoot`集成，注册中心使用`Nacos`。

1. `pom.xml`添加依赖项：
   ```xml
   <dependency>
        <groupId>org.apache.dubbo</groupId>
        <artifactId>dubbo-spring-boot-starter</artifactId>
        <version>2.7.5</version>
   </dependency>
   <dependency>
     <groupId>org.apache.dubbo</groupId>
     <artifactId>dubbo</artifactId>
     <version>2.7.5</version>
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
            queues: 0
            keepAlive: true
        registry: 
            address: nacos://127.0.0.1:8848
   ```
   最新配置项参考[ApplicationConfig](https://github.com/apache/dubbo/blob/master/dubbo-common/src/main/java/org/apache/dubbo/config/ApplicationConfig.java)、[ProtocolConfig](https://github.com/apache/dubbo/blob/master/dubbo-common/src/main/java/org/apache/dubbo/config/ProtocolConfig.java)、[RegistryConfig](https://github.com/apache/dubbo/blob/master/dubbo-common/src/main/java/org/apache/dubbo/config/RegistryConfig.java)、[MonitorConfig](https://github.com/apache/dubbo/blob/master/dubbo-common/src/main/java/org/apache/dubbo/config/MonitorConfig.java)、[ServiceConfig](https://github.com/apache/dubbo/blob/master/dubbo-config/dubbo-config-api/src/main/java/org/apache/dubbo/config/ServiceConfig.java)、[ReferenceConfig](https://github.com/apache/dubbo/blob/master/dubbo-config/dubbo-config-api/src/main/java/org/apache/dubbo/config/ReferenceConfig.java)
3. `SpringBoot`启动类上指定`Dubbo`组件扫描范围：
   ```java
   @Configuration
   @EnableAutoConfiguration
   @ComponentScan(basePackages={"my.demo.service.item"})
   @EnableDubbo(basePackages = { "my.demo.service.item" })
   public class Application {
	   public static void main(String[] args) {
		   new SpringApplicationBuilder(Application.class).web(WebApplicationType.NONE).run(args);
	   }
   }
   ```
4. 暴露`Dubbo`服务的类上使用`@Service`注解（不再需要Spring的`@Component`），引用`Dubbo`服务使用`@Reference`（不再需要Spring的`@Autowired`）；

#### 使用`native-thrift`协议
开源RPC协议中，`Thrift`是效率最好的，在`Dubbo`中可以使用`native-thrift`协议，但比较麻烦，有不少手工工作，还有一些限制，例如服务方法不能返回null等。

1. 以Windows环境为例，下载[thrift-0.12.0.exe](http://archive.apache.org/dist/thrift/0.12.0/thrift-0.12.0.exe)（这是`Dubbo`支持的`Thrift`版本）。
2. 为服务方法编写IDL文件，例如`HelloService.thrift`：
   ```idl
   namespace java thriftdemo.service

   service HelloService {
       string hello( 1:required string name );
   }
   ```
   IDL语法参考[Thrift interface description language](http://thrift.apache.org/docs/idl)、[Thrift Types](http://thrift.apache.org/docs/types)。
3. 使用`Thrift`生成Java类：`thrift-0.12.0.exe -gen java HelloService.thrift`，将Java类拷贝到相应package中。
4. `Dubbo`的provider、consumer都指定protocol name为`native-thrift`，如果直连，协议名称也使用`native-thrift://xxxx:xx`。
5. `Dubbo`的provider和consumer都使用`HelloService.Iface`接口。