APM框架系列：
- [APM之PinPoint部署和使用](https://github.com/liuzhibin-cn/my-demo/blob/master/docs/APM-PinPoint.md)
- [APM之SkyWalking部署和使用](https://github.com/liuzhibin-cn/my-demo/blob/master/docs/APM-SkyWalking.md)
- [APM之ZipKin部署和使用](https://github.com/liuzhibin-cn/my-demo/blob/master/docs/APM-ZipKin.md)

-------------------------
#### ZipKin架构
<img src="https://richie-leo.github.io/ydres/img/10/120/1013/zipkin-architecture.png" style="width:99%;max-width:550px;" />

相关概念：
- 架构组件：
  - [Instrumentation](https://github.com/openzipkin/brave/tree/master/instrumentation)：探针，负责数据采集；
  - [Collector](https://github.com/openzipkin/zipkin/tree/master/zipkin-collector)：ZipKin Server的数据接收器，接收方式支持HTTP、[ActiveMQ](https://github.com/openzipkin/zipkin/tree/master/zipkin-collector/activemq)、[Kafka](https://github.com/openzipkin/zipkin/tree/master/zipkin-collector/kafka)、[RabbitMQ](https://github.com/openzipkin/zipkin/tree/master/zipkin-collector/rabbitmq)，启动ZipKin Server时配置指定，参考[Zipkin Server README](https://github.com/openzipkin/zipkin/blob/master/zipkin-server/README.md)；
  - Transport：数据传输方案，由Collector方案决定；
  - [Storage](https://github.com/openzipkin/zipkin/tree/master/zipkin-storage)：数据存储方案，支持内存、[Cassandra](https://github.com/openzipkin/zipkin/tree/master/zipkin-storage/cassandra)、[ElasticSearch](https://github.com/openzipkin/zipkin/tree/master/zipkin-storage/elasticsearch)、[MySQL](https://github.com/openzipkin/zipkin/tree/master/zipkin-storage/mysql-v1)，启动ZipKin Server时配置指定，参考[Zipkin Server README](https://github.com/openzipkin/zipkin/blob/master/zipkin-server/README.md)；
- 项目组成：
  - [openzipkin/zipkin](https://github.com/openzipkin/zipkin)：ZipKin Server组件，包含Collector、Storage、API、UI；
  - [openzipkin/brave](https://github.com/openzipkin/brave)：Instrumentation探针项目，客户端引用；
  - [openzipkin/zipkin-reporter-java](https://github.com/openzipkin/zipkin-reporter-java)：Reporter指Instrumentation向Collector发送数据的方案，例如[okhttp3](https://github.com/openzipkin/zipkin-reporter-java/tree/master/okhttp3)、[Kafka](https://github.com/openzipkin/zipkin-reporter-java/tree/master/kafka)。首先Collector支持不同数据接收方式，Instrumentation发送方案也不一样；另外同一种Collector数据接收方案，也可以使用不同的客户端框架来完成，这些工作由reporter项目负责；
- 链路跟踪：
  - TraceId：标记一次全链路调用，全局唯一；
  - SpanId：标记一次RPC调用；
  - Annotations：ZipKin跟踪RPC性能的方法，记录4个时间点来反映RPC调用性能细节：Client Start、Server Start、Server Finish、Client Finish，参考功*界面功能 -> 链路跟踪详情*；
  - Propagation：在RPC调用过程中Inject封装、Extract解封跟踪数据，例如[b3-propagation](https://github.com/openzipkin/b3-propagation)：<br />
    <img src="https://richie-leo.github.io/ydres/img/10/120/1013/propagation.png" style="width:99%;max-width:700px;" />

-------------------------
#### 部署ZipKin Server
使用当前[ZipKin Server](https://github.com/openzipkin/zipkin)最新版本[2.19.2](https://github.com/openzipkin/zipkin/releases/tag/2.19.2)，使用[my-demo](https://github.com/liuzhibin-cn/my-demo)作为演示项目。

ZipKin默认按Docker容器部署方式发布，使用Docker或Java快速部署都非常方便。

快速部署（内存存储、HTTP Collector）：
```sh
curl -sSL https://zipkin.io/quickstart.sh | bash -s
java -jar zipkin.jar
```

存储到MySQL（只用到3个表）：
1. MySQL创建zipkin数据库，执行[schema DDL](https://github.com/openzipkin/zipkin/blob/master/zipkin-storage/mysql-v1/src/main/resources/mysql.sql)建表；
2. 启动ZipKin：
   ```sh
   STORAGE_TYPE=mysql MYSQL_HOST=localhost MYSQL_TCP_PORT=3306 MYSQL_DB=zipkin MYSQL_USER=root MYSQL_PASS=dev java -jar zipkin.jar
   ```

访问[http://localhost:9411](http://localhost:9411)查看UI。

-------------------------
#### 客户端应用使用
- `PinPoint`：采用`javaagent`方式，对应用完全无侵入，项目无需添加任何额外依赖项，无需修改代码，这点做得最好；
- `SkyWalking`：采用`javaagent`方式，普通功能对应用无侵入，以下两项功能需要应用稍作修改：
  - 中添加自定义Tag项：应用代码在`SkyWalking Span`中添加自定义Tag，可以按需输出方法参数值等关键信息，辅助应用排错和性能分析，`PinPoint`和`ZipKin`都不支持（可自行实现）；
  - 日志中输出全局跟踪ID，需要添加`SkyWalking`依赖项；
- `ZipKin`：没有采用`javaagent`方式，应用强依赖`ZipKin`，必须打包到应用中与应用一起运行，每个项目需要添加依赖项、配置，不同探针有不同的bean配置需求；

在[my-demo](https://github.com/liuzhibin-cn/my-demo)中运行ZipKin演示：
1. 使用`zipkin`参数编译打包（或者手工打包，使用maven profile `dev,zipkin`）：
   ```sh
   sh $PROJECT_HOME/package.sh zipkin
   ```
2. 按下面脚本顺序启动服务和应用：
   ```sh
   java -jar item-service\target\item-service-0.0.1-SNAPSHOT.jar
   java -jar stock-service\target\stock-service-0.0.1-SNAPSHOT.jar
   java -jar user-service\target\user-service-0.0.1-SNAPSHOT.jar
   java -jar order-service\target\order-service-0.0.1-SNAPSHOT.jar
   java -jar shop-web\target\shop-web-0.0.1-SNAPSHOT.jar
   ```
3. 访问[http://localhost:8090](http://localhost:8090)执行一些操作，即可在PinPoint界面查看结果；

对不同框架Instrumentation使用的拦截方案不同，具体使用方法参考各Instrumentation实现，下面是[my-demo](https://github.com/liuzhibin-cn/my-demo)中用到的几种。

##### SpringBoot Web项目
最简单方案是使用[spring-cloud-sleuth](https://spring.io/projects/spring-cloud-sleuth)（项目不需要是SpringCloud服务，普通SpringBoot Web项目添加`spring-cloud-sleuth`依赖即可），[my-demo](https://github.com/liuzhibin-cn/my-demo)中的`shop-app`配置示例如下：
1. `pom.xml`添加依赖项：
   ```xml
   <dependencies>
       <dependency>
   	      <groupId>org.springframework.cloud</groupId>
   		  <artifactId>spring-cloud-starter-zipkin</artifactId>
       </dependency>
   </dependencies>
   <dependencyManagement>
       <dependencies>
           <dependency>
               <groupId>org.springframework.cloud</groupId>
               <artifactId>spring-cloud-sleuth</artifactId>
               <version>2.2.0.RELEASE</version>
               <type>pom</type>
               <scope>import</scope>
           </dependency>
       </dependencies>
   </dependencyManagement>
   ```
2. `application.yml`添加配置：
   ```yml
   spring:
      zipkin: # spring-cloud-sleuth配置
         base-url: http://192.168.31.108:9411 # ZipKin Server地址
         sleuth:
            sampler:
               percentage: 1.0 # 采样比例，1.0表示100%采样
   ```

底层使用Servlet的Filter实现HTTP拦截，ZipKin Instrumentation的设置工作都由`spring-cloud-sleuth`完成，项目中无需额外处理。

##### Dubbo项目
使用Dubbo的Filter机制实现拦截，Alibaba Dubbo使用[dubbo-rpc instrumentation](https://github.com/openzipkin/brave/tree/master/instrumentation/dubbo-rpc)，Apache Dubbo使用[dubbo instrumentation](https://github.com/openzipkin/brave/tree/master/instrumentation/dubbo)。[my-demo](https://github.com/liuzhibin-cn/my-demo)基于`Alibaba Dubbo 2.6.7`、`dubbo-spring-boot-starter`使用`dubbo-rpc`，设置方法如下：

1. `pom.xml`添加依赖项：
   ```xml
   <dependencies>
	    <dependency>
            <groupId>io.zipkin.brave</groupId>
            <artifactId>brave-instrumentation-dubbo-rpc</artifactId>
        </dependency>
        <dependency>
            <groupId>io.zipkin.reporter2</groupId>
            <artifactId>zipkin-sender-okhttp3</artifactId>
        </dependency>
   </dependencies>
   <dependencyManagement>
		<dependency>
			<groupId>io.zipkin.brave</groupId>
			<artifactId>brave-bom</artifactId>
			<version>5.9.1</version>
			<type>pom</type>
			<scope>import</scope>
		</dependency>
   </dependencyManagement>
   ```
2. 实现`ZipkinProperties`和`ZipkinConfiguration`：
   ```java
    @ConfigurationProperties(prefix="zipkin")
    public class ZipkinProperties {
    	@Value("${dubbo.application.name}")
    	private String serviceName;
    	private String server;
    	private int connectTimeout;
    	private int readTimeout;
    	//getters and setters
    }
    
    @Configuration
    @EnableConfigurationProperties({ZipkinProperties.class})
    public class ZipkinConfiguration {
    	@Autowired
    	ZipkinProperties properties;
    	@Bean
    	public Tracing tracing() { //名称必须为tracing
    		Sender sender = OkHttpSender.create(properties.getServer());
    		AsyncReporter<Span> reporter = AsyncReporter.builder(sender)
    			.closeTimeout(properties.getConnectTimeout(), TimeUnit.MILLISECONDS)
    			.messageTimeout(properties.getReadTimeout(), TimeUnit.MILLISECONDS)
    			.build();
    		Tracing tracing = Tracing.newBuilder()
    			.localServiceName(properties.getServiceName())
    			.propagationFactory(ExtraFieldPropagation.newFactory(B3Propagation.FACTORY, "brave-trace"))
    			.sampler(Sampler.ALWAYS_SAMPLE)
    			.spanReporter(reporter)
    			.build();
    		return tracing;
    	}
    }
   ```
3. `application.yml`添加配置：
   ```yml
   dubbo:
      # 省略了其它dubbo配置
      provider:
         filter: tracing # 对服务提供者启用ZipKin监控
      consumer:
         filter: tracing # 对服务消费者启用ZipKin监控
   zipkin:
      server: http://192.168.31.108:9411/api/v2/spans
      connectTimeout: 2000
      readTimeout: 2000
   ```

实现原理：
1. [`dubbo-rpc/META-INF/dubbo/com.alibaba.dubbo.rpc.Filter`](https://github.com/openzipkin/brave/blob/master/instrumentation/dubbo-rpc/src/main/resources/META-INF/dubbo/com.alibaba.dubbo.rpc.Filter)内容如下：
    ```
    tracing=brave.dubbo.rpc.TracingFilter
    ```
    `provider`和`consumer`上指定的`filter: tracing`为这个SPI扩展的名称，Dubbo根据名称加载`brave.dubbo.rpc.TracingFilter`扩展。
2. [`TracingFilter`](https://github.com/openzipkin/brave/blob/master/instrumentation/dubbo-rpc/src/main/java/brave/dubbo/rpc/TracingFilter.java)的注解说明该filter依赖一个名为`tracing`的`brave.Tracing`对象，即`ZipkinConfiguration`提供的：
   ```java
   @Activate(group = {Constants.PROVIDER, Constants.CONSUMER}, value = "tracing")
   ```
   Dubbo `ExtensionLoader`根据setters（`setTracing(Tracing tracing)`、`setRpcTracing(RpcTracing rpcTracing)`）为filter提供依赖注入。

> `dubbo-rpc 5.9.1`版本在处理一个新的调用链开始、结束方面有些问题。`my-demo`中的`shop-web`，之前并非是web项目，而是普通Java项目直接命令行运行，使用了SpringBoot，执行完`runFullTestCase`后，记录下来的调用链是错乱的，改为web项目，前面使用了`spring-cloud-sleuth`之后就正常了。<br />
> 因为`runFullTestCase`中触发了多个Dubbo服务调用，在第一次调用时整个应用还没有开启跟踪，没有trace上下文，应当开启一个新的跟踪，该方法调用结束时，结束这个跟踪，随后遇到其它Dubbo服务调用同样处理，这样记录下来的调用链就是合理的、结构正确的，这方面处理不当则会造成调用链错乱。

##### MySQL JDBC
通过JDBC的interceptor机制实现，根据`my-demo`使用的`mysql-connector-java`版本选择[brave-instrumentation-mysql8](https://github.com/openzipkin/brave/tree/master/instrumentation/mysql8)：
1. `pom.xml`添加依赖项：
   ```xml
	<dependency>
	    <groupId>io.zipkin.brave</groupId>
	    <artifactId>brave-instrumentation-mysql8</artifactId>
	</dependency>
   ```
2. JDBC url添加参数，指定interceptor：
   ```url
   ?queryInterceptors=brave.mysql8.TracingQueryInterceptor&exceptionInterceptors=brave.mysql8.TracingExceptionInterceptor&zipkinServiceName=db-order
   ```

##### 应用日志中输出全局TraceId
1. `pom.xml`添加依赖项：
   ```xml
   <dependency>
       <groupId>io.zipkin.brave</groupId>
       <artifactId>brave-context-slf4j</artifactId>
   </dependency>
   ```
2. 为`brave.Tracing`配置slf4j的`MDCScopeDecorator`：
   ```java
	Tracing tracing = Tracing.newBuilder()
		.localServiceName(properties.getServiceName())
		.propagationFactory(ExtraFieldPropagation.newFactory(B3Propagation.FACTORY, "brave-trace"))
		.sampler(Sampler.ALWAYS_SAMPLE)
		.spanReporter(reporter)
		//配置slf4j的MDCScopeDecorator
		.currentTraceContext(ThreadLocalCurrentTraceContext.newBuilder().addScopeDecorator(MDCScopeDecorator.create()).build())
		.build();
   ```
3. `logback.xml`通过`%X{traceId}`、`%X{spanId}`输出链路跟踪信息：

<img src="https://richie-leo.github.io/ydres/img/10/120/1013/app-log.jpg" style="width:99%;max-width:820px;" />

-------------------------
#### 界面功能
ZipKin的界面功能最简单，只有依赖图、链路跟踪查询。

依赖图：<br />
<img src="https://richie-leo.github.io/ydres/img/10/120/1013/screen-dependencies.png" style="width:99%;max-width:570px;" />

链路跟踪 - 列表：<br />
<img src="https://richie-leo.github.io/ydres/img/10/120/1013/screen-trace-list.png" style="width:99%;max-width:850px;" />

链路跟踪 - 详情（Dubbo服务调用，右边Annotations上的4个点依次代表Client Start、Server Start、Server Finish、Client Finish，可以清晰的看出服务端执行时间、客户端调用时间、请求和响应的网络传输时间）：<br />
<img src="https://richie-leo.github.io/ydres/img/10/120/1013/screen-trace-detail-dubbo.png" style="width:99%;max-width:850px;" />

链路跟踪 - 详情（Web请求，Tags记录了部分Web请求信息）：<br />
<img src="https://richie-leo.github.io/ydres/img/10/120/1013/screen-trace-detail-web.png" style="width:99%;max-width:850px;" />

链路跟踪 - 详情（JDBC操作，记录了SQL语句）：<br />
<img src="https://richie-leo.github.io/ydres/img/10/120/1013/screen-trace-detail-sql.png" style="width:99%;max-width:850px;" />