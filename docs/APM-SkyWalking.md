----------------------------------
#### SkyWalking架构
<img src="https://richie-leo.github.io/ydres/img/10/120/1011/architecture.jpeg" style="width:99%;max-width:800px;" />

----------------------------------
#### 部署SkyWalking
[Apache SkyWalking](http://skywalking.apache.org/)版本[6.5.0](http://archive.apache.org/dist/skywalking/)，Windows环境单机部署，使用[my-demo](https://github.com/liuzhibin-cn/my-demo)作为演示项目。

存储：
- 演示环境简单起见存储到MySQL，生产环境可以用ElasticSearch、TiDB，存储方案上比PinPoint多；
- 用到了300多个表，主要信息存储在`service_inventory`、`service_instance_inventory`、`endpoint_inventory`、`segment`等表中；

1. 下载[SkyWalking 6.5.0 Windows包](http://archive.apache.org/dist/skywalking/6.5.0/apache-skywalking-apm-6.5.0.zip)，解压；
2. 下载[MySQL Connector/J](https://dev.mysql.com/downloads/connector/j/)放入`oap-libs`，本文使用`8.0.18`版本；
3. 配置：
   1. `config/application.yml`：Backend启动时检查并自动创建MySQL表，无需手工创建；
      - `storage`注释掉`h2`改用`mysql`，设置JDBC连接、用户密码，添加`dataSource.useSSL: false`；
      - `receiver-trace`、`service-mesh`下面的`bufferPath`指定一个绝对路径；
      > 使用默认配置时Mac和Windows环境都发现实际路径有点乱，Mac下两个路径都在skywalking父目录，Windows下一个在父目录一个在skywalking目录；
   2. `webapp/webapp.yml`：Web UI配置，全部采用默认值；
4. 启动：`oapService.bat`启动Backend，`webappService.bat`启动Web UI，`startup.bat`启动所有；
   > SkyWalking使用`start`批处理命令新开cmd窗口启动Backend和Web UI，使用cmd的默认代码页（Win10下为936），导致Console异常和日志信息的中文显示为乱码，本文通过注册表修改cmd默认代码页，可能需要重启才能生效，临时解决办法修改`oapService.bat`：
   > ```
   > @REM start "%OAP_PROCESS_TITLE%" %_EXECJAVA% "%OAP_OPTS%" -cp "%CLASSPATH%" org.apache.skywalking.oap.server.starter.OAPServerStartUp
   > %_EXECJAVA% "%OAP_OPTS%" -cp "%CLASSPATH%" org.apache.skywalking.oap.server.starter.OAPServerStartUp
   > ```

> Mac环境下载[apache-skywalking-apm-6.5.0.tar.gz](http://archive.apache.org/dist/skywalking/6.5.0/apache-skywalking-apm-6.5.0.tar.gz)即可，部署上没有其它特别之处。

----------------------------------
#### 客户端应用使用agent
配置`agent\config\agent.config`：
```
agent.service_name=${SW_AGENT_NAME:unknown}
collector.backend_service=${SW_AGENT_COLLECTOR_BACKEND_SERVICES:192.168.31.108:11800}
# 其它保留默认配置
```

启动应用时通过`-javaagent`指定SkyWalking代理，`-Dskywalking.agent.service_name`指定应用名称：
```sh
java -javaagent:F:\workspace\skywalking\agent\skywalking-agent.jar -Dskywalking.agent.service_name=item-service -jar item-service\target\item-service-0.0.1-SNAPSHOT.jar
java -javaagent:F:\workspace\skywalking\agent\skywalking-agent.jar -Dskywalking.agent.service_name=stock-service -jar stock-service\target\stock-service-0.0.1-SNAPSHOT.jar
java -javaagent:F:\workspace\skywalking\agent\skywalking-agent.jar -Dskywalking.agent.service_name=user-service -jar user-service\target\user-service-0.0.1-SNAPSHOT.jar
java -javaagent:F:\workspace\skywalking\agent\skywalking-agent.jar -Dskywalking.agent.service_name=order-service -jar order-service\target\order-service-0.0.1-SNAPSHOT.jar
java -javaagent:F:\workspace\skywalking\agent\skywalking-agent.jar -Dskywalking.agent.service_name=test-app -jar test-app\target\test-app-0.0.1-SNAPSHOT.jar
```

----------------------------------
#### 全链路跟踪
在全链路跟踪、日志中输出跟踪信息方面：
- PinPoint对应用完全无侵入，无需添加额外依赖项，无需修改代码，这点做得比SkyWalking好；
- SkyWalking支持输出方法参数值；

支持情况参考[Supported middleware, framework and library](https://github.com/apache/skywalking/blob/master/docs/en/setup/service-agent/java-agent/Supported-list.md)，对框架的关键方法进行跟踪，例如Dubbo服务的客户端调用和服务端入口，数据库的`PrepareStatement.execute()`等。

参考[SkyWalking Cross Process Propagation Headers Protocol](https://github.com/apache/skywalking/blob/master/docs/en/protocols/Skywalking-Cross-Process-Propagation-Headers-Protocol-v2.md)、[Trace Data Protocol v2](https://github.com/apache/skywalking/blob/master/docs/en/protocols/Trace-Data-Protocol-v2.md)：
- `TraceId`：标记一个调用链，全局唯一，对应于[Google Dapper](http://research.google.com/pubs/pub36356.html)的`TraceId`；
- `Segment`：调用链上服务实例每次处理RPC请求将生成一个`Segment`；
- `Span`：每次方法调用生成一个`Span`，用父子关系记录方法调用层次关系；

业务代码中未被跟踪的方法，如果需要跟踪，则：
1. pom添加依赖项：
   ```xml
   <dependency>
       <groupId>org.apache.skywalking</groupId>
       <artifactId>apm-toolkit-trace</artifactId>
       <version>6.5.0</version>
   </dependency>
   ```
2. 在方法上添加`@Trace`注解；
   > SkyWalking通过AOP实现跟踪，静态方法添加`@Trace`无效，只能用于实例方法；

----------------------------------
#### 全链路跟踪添加方法参数值
全链路跟踪只记录方法名，不记录参数值，可通过代码添加，在当前span上添加tag（必须是被SkyWalking跟踪的方法）：
```java
ActiveSpan.tag("account", account);
ActiveSpan.tag("userId", String.valueOf(userId));
```

全链路跟踪中即查看tag名称和值：<br />
<img src="https://richie-leo.github.io/ydres/img/10/120/1011/skywalking-span.png" style="width:99%;max-width:800px;" />

记录SQL语句参数值，可以在`agent\config\agent.config`文件中将`plugin.mysql.trace_sql_parameters`设为true。

----------------------------------
#### 应用日志中输出全局`trace-id`
1. pom添加依赖项：
   ```xml
   <dependency>
       <groupId>org.apache.skywalking</groupId>
       <artifactId>apm-toolkit-logback-1.x</artifactId>
       <version>6.5.0</version>
   </dependency>
   ```
2. logback日志layout使用`org.apache.skywalking.apm.toolkit.log.logback.v1.x.TraceIdPatternLogbackLayout`，通过`%TID`输出`trace-id`：
   ```xml
   <appender name="APP" class="ch.qos.logback.core.ConsoleAppender">
       <encoder class="ch.qos.logback.core.encoder.LayoutWrappingEncoder">
           <charset>UTF-8</charset>
           <layout class="org.apache.skywalking.apm.toolkit.log.logback.v1.x.TraceIdPatternLogbackLayout">
               <pattern>${CONSOLE_LOG_PATTERN:-%clr([%d{${LOG_DATEFORMAT_PATTERN:-yyMMdd HH:mm:ss.SSS}}]){faint} %clr(${LOG_LEVEL_PATTERN:-%5p}) %clr(${PID:- }){magenta} %clr(%tid){yellow}%clr(:){faint} %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}}</pattern>
           </layout>
       </encoder>
   </appender>
   ```

<img src="https://richie-leo.github.io/ydres/img/10/120/1011/app-log.jpg?raw=true" style="width:99%;max-width:800px;" />  <br />
未被跟踪的方法`trace-id`输出`TID:N/A`

----------------------------------
#### 界面功能
SkyWalking和PinPoint界面功能相差不大：
- PinPoint界面稍微细腻些，Agent Inspector、Application Inspector界面信息集中度更高，适合大屏监控；
- SkyWalking提供数据库访问统计界面，基于服务/应用、实例层级的汇总统计，比基于数据库的整体统计粒度更新更精准，PinPoint没有；


[Service Dashboard] -> [Service]：针对服务的性能汇总统计：**平均响应时间**、**吞吐率**、**服务实例清单及吞吐率** <br />
<img src="https://richie-leo.github.io/ydres/img/10/120/1011/skywalking-screen-service.png" style="width:99%;max-width:900px;" />

[Service Dashboard] -> [Endpoint]：针对服务方法的性能汇总统计：**平均响应时间**、**吞吐率**、**依赖关系图**、**慢方法排行(平均响应时间)**、**慢方法排行(单次调用)** <br />
<img src="https://richie-leo.github.io/ydres/img/10/120/1011/skywalking-screen-endpoint.png" style="width:99%;max-width:900px;" />

[Service Dashboard] -> [Instance]：针对服务实例的性能汇总统计，服务实例所在JVM、CLR的性能监控情况：**平均响应时间**、**吞吐率**、**JVM堆内存**、**JVM非堆内存**、**JVM GC**、**CPU** <br />
<img src="https://richie-leo.github.io/ydres/img/10/120/1011/skywalking-screen-instance.png" style="width:99%;max-width:900px;" />

[Database Dashboard] -> [Database]：针对数据库的性能汇总统计（基于JDBC数据库操作采集到的数据）：**响应时间**、**吞吐率**、**慢查询排行** <br />
<img src="https://richie-leo.github.io/ydres/img/10/120/1011/skywalking-screen-database.png" style="width:99%;max-width:900px;" />

全链路跟踪、性能分析：

表格方式展示：<br />
<img src="https://richie-leo.github.io/ydres/img/10/120/1011/skywalking-screen-trace-table.png" style="width:99%;max-width:900px;" />

树状方式展示：<br />
<img src="https://richie-leo.github.io/ydres/img/10/120/1011/skywalking-screen-trace-tree.png" style="width:99%;max-width:900px;" />
