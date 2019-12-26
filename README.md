-------------------------------------------------------------------
#### 演示项目架构
![](docs/images/architecture.png) <br />

-------------------------------------------------------------------
#### 运行演示项目
[package.sh](https://github.com/liuzhibin-cn/my-demo/blob/master/package.sh)为打包脚本：
- `sh package.sh`：最简单运行方式，使用单个MySQL数据库、[nacos](https://nacos.io/)注册中心，运行4个[Dubbo](http://dubbo.apache.org/zh-cn/)服务和1个Web应用；
- `sh package.sh -mycat`：使用[Mycat](http://www.mycat.io/)分库分表；
- `sh package.sh -sharding-proxy`：使用[Sharding-Proxy](https://shardingsphere.apache.org/)分库分表；
- `sh package.sh -seata`：使用[Seata](http://seata.io/zh-cn/)分布式事务管理；
- `sh package.sh -zipkin`：使用[ZipKin](https://github.com/openzipkin/zipkin)进行链路跟踪、性能分析；
- `sh package.sh -pinpoint`：使用[PinPoint](https://github.com/naver/pinpoint)进行链路跟踪、性能分析；
- `sh package.sh -skywalking`：使用[SkyWalking](http://skywalking.apache.org/)进行链路跟踪、性能分析；

参数可以组合，例如`sh package.sh -mycat -seata -zipkin`，分库分表参数只能二选一，APM工具只能三选一。

最简单运行方式操作步骤：
1. JDK 8+；
2. 部署nacos，用于Dubbo注册中心；<br />
   比较简单，参考[Nacos快速开始](https://nacos.io/zh-cn/docs/quick-start.html)即可。
3. MySQL数据库；<br />
   建库脚本[sql-schema.sql](https://github.com/liuzhibin-cn/my-demo/blob/master/docs/sql-schema.sql)，是演示分库分表用的建库脚本，简单方式运行只需要其中`mydemo-dn1`单库即可。
2. 修改项目配置信息；<br />
   配置信息都在[parent pom.xml](https://github.com/liuzhibin-cn/my-demo/blob/master/pom.xml)中，包括数据库连接信息、nacos地址等。
3. 编译打包；<br />
   执行`sh package.sh`，Windows环境装了git bash就可以运行。
4. 运行演示项目：<br />
   依次启动服务和Web应用:
   ```sh
   java -jar item-service\target\item-service-0.0.1-SNAPSHOT.jar
   java -jar stock-service\target\stock-service-0.0.1-SNAPSHOT.jar
   java -jar user-service\target\user-service-0.0.1-SNAPSHOT.jar
   java -jar order-service\target\order-service-0.0.1-SNAPSHOT.jar
   java -jar shop-web\target\shop-web-0.0.1-SNAPSHOT.jar
   ```
6. 通过[http://localhost:8090/shop](http://localhost:8090/shop)访问，执行操作查看效果；

#### 分布式事务管理
阿里云分布式事务管理GTS的开源版Seata，2019年1月开源出来，1.0.0版已经发布。相关概念、部署和使用方法参考[Seata分布式事务管理框架概览](https://github.com/liuzhibin-cn/my-demo/blob/master/docs/Seata-Distributed-Transaction-Management.md)。

Seata提供AT、TCC、Saga三种柔性事务模式，AT模式对应用几乎透明，使用方便。但目前来看，性能开销还比较高。

#### 数据库分库分表
本项目演示了使用[Mycat](http://www.mycat.io/)和[Sharding-Proxy](https://shardingsphere.apache.org/)进行分库分表，相关概念、部署和使用方法，参考[MyCat分库分表概览](https://github.com/liuzhibin-cn/my-demo/blob/master/docs/Sharding-Mycat-Overview-Quickstart.md)、[Sharding-Proxy分库分表概览](https://github.com/liuzhibin-cn/my-demo/blob/master/docs/Sharding-Sharding-Proxy-Overview-Quickstart.md)，这2个分库分表开源方案与[DRDS](https://help.aliyun.com/document_detail/118010.html)对比，参考[DRDS产品概览](https://github.com/liuzhibin-cn/my-demo/blob/master/docs/Sharding-DRDS-Overview.md)。

Mycat、Sharding-Proxy和DRDS都实现了MySQL协议，成为独立的中间件，将分库分表、读写分离等数据存储的弹性伸缩方案与应用隔离，并且实现语言无关。

#### APM全链路监控
演示项目支持[PinPoint](https://github.com/naver/pinpoint)、[SkyWalking](http://skywalking.apache.org/)、[ZipKin](https://zipkin.io/)三种APM工具进行全链路跟踪和性能分析，相关概念、部署和使用方法，参考[PinPoint部署和使用](https://github.com/liuzhibin-cn/my-demo/blob/master/docs/APM-PinPoint.md)、[SkyWalking部署和使用](https://github.com/liuzhibin-cn/my-demo/blob/master/docs/APM-SkyWalking.md)、[ZipKin部署和使用](https://github.com/liuzhibin-cn/my-demo/blob/master/docs/APM-ZipKin.md)。

三种APM工具对比：
- 使用方式：PinPoint和SkyWalking都采用javaagent方式，对应用代码几乎没有侵入性；ZipKin需要和应用打包到一起，并在应用中完成各种配置，属于强依赖关系；
- 链路跟踪能力：整体上看相差不大，基本都参照[Google Dapper](http://research.google.com/pubs/pub36356.html)，也都支持对大量主流框架的跟踪，细节上有些差异：
  - 对单次RPC调用分析，ZipKin定义的Annotations更精细，参考[ZipKin部署和使用](https://github.com/liuzhibin-cn/my-demo/blob/master/docs/APM-ZipKin.md)；
  - PinPoint和SkyWalking都提供将额外方法添加到调用链跟踪的功能，其中PinPoint对代码完全没有侵入性，SkyWalking则需要对方法添加注解；
  - SkyWalking支持在Span中添加自定义tag功能，利用该功能可以将方法参数值等额外信息记录到Span中，有利于问题分析；
- UI功能：PinPoint和SkyWalking UI功能比较丰富，都提供应用/服务、实例等层级的性能统计，两者各有特色；ZipKin UI功能最弱，只提供依赖关系、具体调用链查看分析；<br />
  额外的UI功能，可以读取APM工具的数据，自定义开发；
- 社区支持：ZipKin架构灵活、文档完善，社区支持度最高，Spring Cloud和Service Mesh（[istio](https://github.com/istio/)）官方提供ZipKin支持；SkyWalking是华为员工开发，已成为Apache项目，从官方文档的英文水平看，在国外不一定能获得太高支持；PinPoint为韩国公司开源；
