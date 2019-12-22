#### 项目情况
[ShardingSphere](https://shardingsphere.apache.org/)前身是当当网开源的Sharding-JDBC，2018年京东选择将它作为云化数据库中间件（对标阿里云的DRDS），加大投入力度，合作成立社区，更名为ShardingSphere，加入Apache孵化项目。目前包括：
- [Sharding-JDBC](https://shardingsphere.apache.org/document/current/cn/manual/sharding-jdbc/)：客户端分库分表组件；<br />
  <img src="https://richie-leo.github.io/ydres/img/10/120/1015/sharding-jdbc-brief.jpg" style="max-width:300px;" />
- [Sharding-Proxy](https://shardingsphere.apache.org/document/current/cn/manual/sharding-proxy/)：实现MySQL协议，面向客户端语言无关，本身采用`Sharding-JDBC`与后端数据库通讯，总体架构方案上与Mycat、DRDS类似；<br />
  <img src="https://richie-leo.github.io/ydres/img/10/120/1015/sharding-proxy-brief_v2.jpg" style="max-width:560px;" />
- [Sharding-Sidecar](https://shardingsphere.apache.org/document/current/cn/manual/sharding-sidecar/)：开发中尚未发布，类似Service Mesh的Sidecar，实现Database Mesh能力；<br />
  <img src="https://richie-leo.github.io/ydres/img/10/120/1015/sharding-sidecar-brief_v2.jpg" style="max-width:560px;" />
- [Sharding-UI](https://shardingsphere.apache.org/document/current/cn/manual/sharding-ui/)：提供界面管理功能，通过Zookeeper动态管理`Sharding-Proxy`的配置；

#### Sharding-Proxy相关概念
- 分片相关，参考[核心概念-SQL](https://shardingsphere.apache.org/document/current/cn/features/sharding/concept/sql/)、[核心概念-分片](https://shardingsphere.apache.org/document/current/cn/features/sharding/concept/sharding/)：
  - 分库：将数据分片存入不同的数据库schema；
  - 分表：将数据分片存入同一个数据库schema下不同的表中，例如`t_order_0`、``t_order_1`；
    > 这与行业常说的分库分表有区别，通常所说的分库指按微服务、业务方式垂直拆分，分表指对某些表进行水平拆分。<br />
    > Sharding-Proxy的分表是个特有概念。在解决逻辑分片与物理存储对应关系方面，DRDS和Mycat都采用在同一个MySQL实例下创建多个schema，每个schema作为dataNode对应一个逻辑分片。数据量或访问压力增加时，增加MySQL实例（服务器），迁移数据重新平衡。这种方案不涉及分片规则调整，以schema为单位的数据迁移方案很多。整体看，Sharding-Proxy的分表带来的作用有限，不是一个必要功能。
  - 数据源：指分片规则中的dataSources配置，与数据库schema对应；
  - 数据节点：数据分片的最小单元，数据源和表组成，例如：`ds_0.t_order_0`, `ds_0.t_order_1`；
  - 绑定表：父子关系表，分片字段和规则一样，关联数据落在同一个分片上；
  - 广播表：每个数据源中都存在的表，表结构和数据完全一样；
- 路由：参考[内核剖析-路由引擎](https://shardingsphere.apache.org/document/current/cn/features/sharding/principle/route/)<br />
  <img src="https://richie-leo.github.io/ydres/img/10/120/1015/route_architecture.jpg" style="max-width:400px;" />
  - 直接路由：客户端通过hint指定分片键值。分片规则必须为Hint方式，且不分表；
  - 笛卡尔路由：指关联查询无法确定分片（非绑定表），必须按2个表数据节点的笛卡尔积组合执行；
  - 广播路由：不能确定分片的单表查询：
    - 全库表路由：在所有数据节点上执行，例如DDL、不能确定分片的DML等；
    - 全库路由：路由到每个数据库schema，例如schema层级的库设置SET命令、事务控制语句等；
    - 全实例路由：路由到每个数据库实例，例如授权语句等；
  - 单播路由：只需要路由到任意一个数据节点，例如`desc t_order`语句；
  - 阻断路由：不发送到后端数据库执行，例如`use db_order`，因为Sharding-Proxy给客户端展示的是逻辑库，后端数据库上可能根本不存在这个schema；
- SQL改写：参考[内核剖析-改写引擎](https://shardingsphere.apache.org/document/current/cn/features/sharding/principle/rewrite/)，例如：
  - 补列：客户端`select avg(price)`，proxy在各分片上执行`select sum(price) as sum_price, count(price) as count_price`，结果集汇总后计算`avg(price)`；
  - 批量拆分：`INSERT INTO t_order(order_id, xxx) VALUES(1, 'xxx'), (2, 'xxx'), ...;`，必须拆分后发给分片节点；
  - 分页改写：`limit 180, 20`改写为`limit 0, 200`，内存中合并排序后再取分页数据；
- 连接模式：参考[内核剖析-执行引擎](https://shardingsphere.apache.org/document/current/cn/features/sharding/principle/execute/)，因为有分表，同一个schema下可能有多个分表存在，为了平衡数据库连接资源而设计的解决方案：
  - 连接限制模式：对同一个数据节点下的分表，只使用一个连接，串行在各分表上执行；对不同数据节点仍采用多线程执行。这种模式在结果集合并时，不能采用基于游标的流式合并，只能将每个分片数据全部读取到内存，再进行合并；
  - 内存限制模式：不限制连接数，同一个数据节点中有多个分表，使用多线程、多链接并行执行。处理结果集合并时，可以采用基于游标的流式合并，节约内存开销；
- 归并引擎：参考[内核剖析-归并引擎](https://shardingsphere.apache.org/document/current/cn/features/sharding/principle/merge/)，处理结果集合并。<br />
  <img src="https://richie-leo.github.io/ydres/img/10/120/1015/merge_architecture_cn.jpg" style="max-width:450px;" />
  - 流式合并：前提是各分片返回的数据是有序的，使用游标，每次next取数即可完成合并操作，避免将全部数据加载到内存后再合并。
- 分布式事务：参考[分布式事务](https://shardingsphere.apache.org/document/current/cn/features/transaction/)，支持XA事务、Saga柔性事务、[Seata柔性事务](http://seata.io/zh-cn/index.html)（阿里2019开源的分布式事务框架）；

#### 演示方案说明
使用[my-demo](https://github.com/liuzhibin-cn/my-demo)项目作为演示，演示环境和详细方案参考[MyCat分库分表概览](https://github.com/liuzhibin-cn/my-demo/blob/master/docs/Sharding-Mycat-Overview-Quickstart.md)。

#### 部署Sharding-Proxy
使用[Sharding-Proxy 4.0.0-RC3](https://www.apache.org/dyn/closer.cgi?path=incubator/shardingsphere/4.0.0-RC3/apache-shardingsphere-incubating-4.0.0-RC3-sharding-proxy-bin.tar.gz)：

1. 下载解压。
   > 注意：Windows环境用WinRAR解压会将文件名较长的截断，导致启动时无法加载相关jar，报找不到Java Main Class错误。需要使用tar解压。
2. 下载[mysql-connector-java-5.1.47.tar.gz](https://cdn.mysql.com//Downloads/Connector-J/mysql-connector-java-5.1.47.tar.gz)，将`mysql-connector-java-5.1.47.jar`拷贝到`lib`目录。
   > 使用`MySQL Connector/J 8.0`以上版本会报错，改回官方使用的版本。
3. 配置分库分表规则。实现[MyCat分库分表概览](https://github.com/liuzhibin-cn/my-demo/blob/master/docs/Sharding-Mycat-Overview-Quickstart.md)同等效果的规则配置如下（详细配置文件参考[docs/sharding-proxy-conf](https://github.com/liuzhibin-cn/my-demo/tree/master/docs/sharding-proxy-conf)）：
   - `server.yaml`
     ```yaml
     authentication:
       users: # 定义逻辑库用户密码
         root: # 用户名：root，可访问所有逻辑库
           password: 123
         mydemo: # 用户名：mydemo
           password: mydemo
           authorizedSchemas: db_user, db_order # 只能访问指定的逻辑库
     props:
       acceptor.size: 16  # 接收客户端请求的工作线程数，默认CPU核数*2
       proxy.transaction.type: LOCAL
       sql.show: true
     ```
   - `config-user.yaml`
     ```yaml
     schemaName: db_user # 逻辑库名称
     dataSources: #数据源配置，可配置多个
       ds_0:
         url: jdbc:mysql://127.0.0.1:3306/mydemo-dn1?characterEncoding=utf8&useTimezone=true&serverTimezone=Asia/Shanghai&useSSL=false
         username: root
         password: 1234
         connectionTimeoutMilliseconds: 3000 #连接超时毫秒数
         idleTimeoutMilliseconds: 60000 #空闲连接回收超时毫秒数
         maxLifetimeMilliseconds: 1800000 #连接最大存活时间毫秒数
         maxPoolSize: 3
       ds_1: # 配置同上，省略
     shardingRule:
       tables:
         usr_user:
           actualDataNodes: ds_${0..1}.usr_user
           databaseStrategy:
             inline: 
               shardingColumn: user_id
               algorithmExpression: ds_${user_id % 2} # 简单使用inline表达式分片
         usr_user_account:
           actualDataNodes: ds_${0..1}.usr_user_account
           databaseStrategy:
             inline: 
               shardingColumn: account_hash
               algorithmExpression: ds_${account_hash % 2}
           keyGenerator:
             type: SNOWFLAKE
             column: user_id
             props: 
               worker.id: 1
               max.tolerate.time.difference.milliseconds: 600000 # 允许的系统时钟回拨10分钟
     ```
   - `config-order.yaml`
     ```yaml
     schemaName: db_order
     dataSources: #数据源配置，可配置多个
       ds_0:
         url: jdbc:mysql://127.0.0.1:3306/mydemo-dn1?characterEncoding=utf8&useTimezone=true&serverTimezone=Asia/Shanghai&useSSL=false
         username: root
         password: 1234
         connectionTimeoutMilliseconds: 3000 #连接超时毫秒数
         idleTimeoutMilliseconds: 60000 #空闲连接回收超时毫秒数
         maxLifetimeMilliseconds: 1800000 #连接最大存活时间毫秒数
         maxPoolSize: 3
       # ds_1, ds_2, ds_3 配置同上，省略
     shardingRule:
       tables:
         ord_order:
           actualDataNodes: ds_${0..3}.ord_order
           databaseStrategy:
             inline: 
               shardingColumn: order_id
               algorithmExpression: ds_${order_id % 4}
         ord_order_item:
           actualDataNodes: ds_${0..3}.ord_order_item
           databaseStrategy:
             inline: 
               shardingColumn: order_id
               algorithmExpression: ds_${order_id % 4}
           keyGenerator:
             type: SNOWFLAKE
             column: order_item_id
             props: 
               worker.id: 1
               max.tolerate.time.difference.milliseconds: 600000 # 允许的系统时钟回拨10分钟
         ord_user_order:
           actualDataNodes: ds_${0..3}.ord_user_order
           databaseStrategy:
             inline: 
               shardingColumn: user_id
               algorithmExpression: ds_${user_id % 17 % 4} # SNOWFLAKE生成的user_id按4取模会导致数据分布不平衡，所以先按17取模
       bindingTables:
         - ord_order,ord_order_item
     ```
4. 启动`Sharding-Proxy`：<br />
   ```sh
   bin\start.bat [port]
   ```
   未指定端口号，默认3307

启动后即可用mysql客户端连接3307端口，对逻辑库进行操作验证。

#### 使用Sharding-Proxy
对[my-demo](https://github.com/liuzhibin-cn/my-demo)项目稍作修改即可使用`Sharding-Proxy`：
1. `parent pom.xml`中MySQL端口号修改为`3307`（`Sharding-Proxy`端口号）；
2. `parent pom.xml`中将`mysql-connector-java`版本改为5.1.47；
   > `Sharding-Proxy`使用的`MySQL Connect/J`版本较低，使用`8.0`以上版本否则会报错。
3. `user-service`和`order-service`修改（主要是降低`mysql-connector-java`版本的修改，及`Mycat`和`Sharding-Proxy`全局序列使用方式不同）：
   - `application.yml`
     ```yml
     datasource:
       name: ${application.name}-ds
       type: org.apache.tomcat.jdbc.pool.DataSource 
       # for Mycat
       # driver-class-name: com.mysql.cj.jdbc.Driver
       #url: jdbc:mysql://${mysql.host}:${mysql.port}/db_user?connectTimeout=3000&socketTimeout=10000&characterEncoding=utf8&useTimezone=true&serverTimezone=Asia/Shanghai&zeroDateTimeBehavior=CONVERT_TO_NULL&useSSL=false${jdbc.interceptors}
       # for Sharding-Proxy
       driver-class-name: com.mysql.jdbc.Driver  
       url: jdbc:mysql://${mysql.host}:${mysql.port}/db_user?connectTimeout=3000&socketTimeout=10000&characterEncoding=utf8&useTimezone=true&serverTimezone=Asia/Shanghai&zeroDateTimeBehavior=convertToNull&useSSL=false${jdbc.interceptors}
     ```
   - `UserDao`
     ```java
 	 //SQL for Mycat
	 //@Insert("insert into usr_user_account(account, password, user_id, account_hash) values (#{account}, #{password}, next value for MYCATSEQ_USER, #{accountHash})")
	 //SQL for Sharding-Proxy
	 @Insert("insert into usr_user_account(account, password, account_hash) values (#{account}, #{password}, #{accountHash})")
	 @SelectKey(before=false, keyColumn="user_id", keyProperty="userId", resultType=Long.class
		, statementType=StatementType.PREPARED
    	, statement="select user_id from usr_user_account where account=#{account} and account_hash=#{accountHash}")
	 int createUserAccount(UserAccount userAccount);
     ```
   - `OrderDao`
     ```java
	 //SQL for Mycat
	 //@Insert("insert into ord_order_item (order_item_id, order_id, item_id, title, quantity, price, subtotal, discount, created_at) " 
	 //		+ "values(next value for MYCATSEQ_ORDERDETAIL, #{orderId}, #{itemId}, #{title}, #{quantity}, #{price}, #{subtotal}, #{discount}, #{createdAt})")
	 //SQL for Sharding-Proxy
	 @Insert("insert into ord_order_item (order_id, item_id, title, quantity, price, subtotal, discount, created_at) " 
			+ "values(#{orderId}, #{itemId}, #{title}, #{quantity}, #{price}, #{subtotal}, #{discount}, #{createdAt})")
	 int createOrderItem(OrderItem orderItem);
     ```

修改后即可重新打包运行项目，查看测试效果。

使用限制参考：[JDBC不支持项](https://shardingsphere.apache.org/document/current/cn/manual/sharding-jdbc/unsupported-items/)、[SQL支持情况](https://shardingsphere.apache.org/document/current/cn/features/sharding/use-norms/sql/)、[分页性能](https://shardingsphere.apache.org/document/current/cn/features/sharding/use-norms/pagination/)。代码中使用的SQL，最基本的CRUD应该没问题，涉及到子查询、表达式等，必须先进行测试，例如：
```sql
select m, count(m) as cnt from (select (user_id % 3) as m from usr_user) t group by m;
```
Sharding-Proxy和Mycat执行结果分别如下（因为Sharding-Proxy对子查询支持很弱造成）：
```
Sharding-Proxy         MyCat
+------+------+    +------+-----+
| m    | cnt  |    | m    | cnt |
+------+------+    +------+-----+
|    0 |    7 |    |    0 |  13 |
|    1 |   12 |    |    1 |  19 |
|    2 |    3 |    |    2 |  12 |
|    0 |    6 |    +------+-----+
|    1 |    7 |
|    2 |    9 |
+------+------+
```

#### 发现的问题
- Sharding-Proxy支持的`MySQL Connector/J`版本较低，MySQL官方建议针对MySQL Server 5.5, 5.6, 5.7, 8.0都使用`MySQL Connector/J 8.0`版本；
- Mycat的SQL解析能力比Sharding-Proxy好些；
- 从演示效果看，Mycat性能比Sharding-Proxy快1倍以上；