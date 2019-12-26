----------------------------------------
#### Seata全局事务与Mycat、Sharding-Proxy分布式事务的区别
<img src="https://richie-leo.github.io/ydres/img/10/120/1017/seata-gtx-concept.png" style="max-width:500px;" />

Mycat、Sharding-Proxy都有分布式事务管理机制，但局限于服务/应用内部，单个前端连接、同一个逻辑库内。<br />
Seata全局事务解决跨服务、跨系统的分布式事务问题，例如订单服务创建订单时，需要调用库存服务预扣库存，调用会员服务扣减账户积分等，即使订单、库存、会员在同一个Mycat逻辑库中，也无法使用Mycat的分布式事务来保证数据一致性，因为跨服务了，由不同JVM进程与Mycat建立数据库连接。这种情况可以用Seata解决。

Seata实现了几种柔性事务方案，不是强一致性，只提供最终一致性。

----------------------------------------
#### Seata事务模式
##### AT模式
参考[Seata AT模式](http://seata.io/zh-cn/docs/dev/mode/at-mode.html)，主要过程：
1. 开启全局事务，生成全局事务XID。<br />
   全局事务将在应用和服务之间传播，原理与APM链路跟踪相同，目前支持Dubbo、SpringBoot、Spring Cloud等。
2. 在全局事务作用范围内，应用和服务开启本地事务，执行业务操作。<br />
   Seata通过`DataSourceProxy`实现拦截，在本地事务执行过程中记录事务日志：
   - 每次数据库操作生成一个事务分支，记录在seata-server的`branch_table`中；
   - 每个事务分支生成一条回滚日志，记录在当前业务库的`undo_log`表中，其中包含`BeforeImage`、`AfterImage`，用于异常时自动生成回滚补偿SQL；
3. 本地事务提交。<br />
   本地事务提交前，Seata必须拿到相应的全局锁，未获得全局锁本地事务不能提交。<br />
   全局锁为行锁级别，记录在seata-server的`lock_table`中。事务修改的所有数据行，在`lock_table`都记录一条行锁记录。如果某条数据被其它事务加了全局锁，则本次事务必须等待该数据的全局锁释放。
4. 全局事务提交、回滚。<br />
   所有本地事务全部成功则提交全局事务，seata-server释放全局锁，异步批量清除回滚日志等。<br />
   如果某个本地事务失败则全局事务回滚，seata-server根据回滚日志，为已经提交的本地事务生成回滚补偿SQL，自动回滚。

下面是两个并发事务`tx1`、`tx2`正常提交、冲突回滚情况示意图：<br />
<img src="https://richie-leo.github.io/ydres/img/10/120/1017/seata_at-1.png" style="max-width:500px;" />

注意：下图`tx1`在开始回滚（全局回滚）时与`tx2`形成死锁，因此必须等待，最终`tx2`锁等待超时，回滚本地事务释放本地锁，`tx1`获得本地锁后执行全局回滚。<br />
<img src="https://richie-leo.github.io/ydres/img/10/120/1017/seata_at-2.png" style="max-width:520px;" />

AT模式本地事务隔离级别由参与方自行定义，全局隔离级别默认是Read Uncommitted，如果要求全局Read Committed，Seata通过对`SELECT FOR UPDATE`代理实现，该语句会导致申请全局锁，如果全局锁被其他事务持有，则释放本地锁（回滚`SELECT FOR UPDATE`语句的本地执行）并重试。这个过程中，查询是被block住的，直到全局锁拿到，即读取的相关数据是已提交的，才返回。
> Seata采用拦截SQL执行方式实现，对用户代码而言，Seata没有获得全局锁，`SELECT FOR UPDATE`语句将一直等待，得到返回结果即代表Seata已经获得全局锁。

<img src="https://richie-leo.github.io/ydres/img/10/120/1017/seata_at-3.png" style="max-width:500px;" />

==注意==：
1. ==事务中只能更新少量数据，不能用于大批量数据更新。从事务机制可以看到，事务管理开销很高==；
2. ==在本地数据库事务基础上，又添加了全局锁管理，需要格外谨慎处理阻塞、死锁问题==；

##### TCC模式
参考[Seata TCC模式](http://seata.io/zh-cn/docs/dev/mode/tcc-mode.html)。TCC即Try-Confirm-Cancel，主要过程如下：
1. 在入口向TC开启全局事务；
2. 全局事务各参与方向TC注册事务分支，执行Prepare方法，根据执行结果向TC报告分支状态；<br />
   类似XA 2PC，各参与方Prepare确认后必须确保能够Commit成功。<br />
   实际运用中，Prepare过程进行各种校验、资源预留等操作。以用户下单场景作为示例，假设用户使用了积分进行抵扣，另外下单时只预扣/锁定库存，实际库存扣减发生在发货出库时：
   - Prepare阶段可以完成库存预扣、用户积分账户预扣等，即各种业务检查和预留；
   - Commit阶段创建订单，关联、使用Prepare阶段预留的资源。Commit阶段不能出现业务条件不满足无法下单的情况（这些全部在Prepare完成），如果因为网络、系统故障导致下单失败，通过重试Commit完成；
   - Rollback阶段则将Prepare中预留的所有资源释放，遇到网络、系统故障同样重试Rollback；
3. 所有参与方Prepare完成后，根据结果确定全局提交或回滚，TC调用各参与方Commit或者Rollback方法；

<img src="https://richie-leo.github.io/ydres/img/10/120/1017/seata_tcc-1.png" style="max-width:600px;" />

TCC模式中，Prepare、Commit、Rollback都是各参与方自定义的函数/逻辑，Seata只负责事务框架和协调。<br />
- AT模式全局事务管理完全自动化，包括回滚操作，无需业务代码介入，前提必须是Java项目，且事务管理仅针对关系数据库。
- TCC模式Try、Confirm、Cancel全部由业务代码实现，但适用范围更广，例如银行间跨行转账这种多方系统集成，适合采用TCC、SAGA模式。

##### Saga模式
参考[SEATA Saga模式](http://seata.io/zh-cn/docs/dev/mode/saga-mode.html)。

Saga模式是SEATA提供的长事务解决方案，在Saga模式中，业务流程中每个参与者都提交本地事务，当出现某一个参与者失败则补偿前面已经成功的参与者，一阶段正向服务和二阶段补偿服务都由业务开发实现。

目前SEATA提供的Saga模式是基于状态机引擎来实现的，机制是：
1. 通过状态图来定义服务调用的流程并生成 json 状态语言定义文件
2. 状态图中一个节点可以是调用一个服务，节点可以配置它的补偿节点
3. 状态图 json 由状态机引擎驱动执行，当出现异常时状态引擎反向执行已成功节点对应的补偿节点将事务回滚
   > 注意: 异常发生时是否进行补偿也可由用户自定义决定
4. 可以实现服务编排需求，支持单项选择、并发、子流程、参数转换、参数映射、服务执行状态判断、异常捕获等功能

<img src="https://richie-leo.github.io/ydres/img/10/120/1017/saga_engine_mechanism.png" style="max-width:700px;" />

仍以前面用户下单场景为例，Saga执行步骤如下：
1. 创建订单；
2. 预扣库存；
3. 积分账户扣减积分；
4. 其它操作；

回滚时取消订单、取消库存预扣、积分账户返还积分等。<br />
与TCC的区别是：TCC一阶段的Prepare全部成功后，订单并没有创建，或者创建得不完整，而Saga的一阶段成功后，订单已经完全创建好了。

----------------------------------------
#### Seata部署
[Seata 1.0.0-GA](https://github.com/seata/seata/releases/tag/v1.0.0)刚发布，本文使用这个版本，nacos和seata以及它们使用的MySQL库都部署在Mac环境（`IP: 192.168.31.108`），使用nacos作为注册中心、配置中心。

1. 下载解压[seata-server-1.0.0.tar.gz](https://github.com/seata/seata/releases/download/v1.0.0/seata-server-1.0.0.tar.gz)；
2. 部署配置nacos：<br />
   下载[config.txt](https://github.com/seata/seata/blob/develop/script/config-center/config.txt)、[nacos-config.sh](https://github.com/seata/seata/blob/develop/script/config-center/nacos/nacos-config.sh)，修改`config.txt`内容，使用`./nacos-config.sh nacos-host:nacos-port`将配置项导入到nacos中。本示例对`config.txt`修改项如下：
   ```sh
   service.vgroup_mapping.my_demo_gtx=default    # 将服务分组名称修改为my_demo_gtx
   service.default.grouplist=192.168.31.108:8091 # Seata服务地址
   store.mode=db # 采用MySQL存储Seata事务信息
   # MySQL连接信息
   store.db.datasource=dbcp
   store.db.db-type=mysql
   store.db.driver-class-name=com.mysql.jdbc.Driver
   store.db.url=jdbc:mysql://127.0.0.1:3306/seata?useUnicode=true
   store.db.user=root
   store.db.password=dev
   # Seata事务信息表名
   store.db.global.table=global_table
   store.db.branch.table=branch_table
   store.db.lock-table=lock_table
   client.undo.log.table=undo_log
   # 客户端禁用Spring datasource自动代理，改为手动配置方式
   client.support.spring.datasource.autoproxy=false
   ```
   导入后可以登录nacos查看相关配置项。
3. MySQL配置：<br />
   下载[mysql.sql](https://github.com/seata/seata/blob/develop/script/server/db/mysql.sql)，在MySQL中建`seata`库，执行`mysql.sql`创建Seata事务信息表。
   > Seata在事务提交和回滚后，会很快清除全局事务信息，为了便于观察，可以添加历史表，通过trigger保留全局事务数据，参考[seata-triggers.sql](https://github.com/liuzhibin-cn/my-demo/blob/master/docs/seata-triggers.sql)。
4. 配置Seata：<br />
   `conf/registry.conf`：
   ```nginx
   registry {  
     type = "nacos" # file、nacos、eureka、redis、zk、consul、etcd3、sofa
     nacos {
       serverAddr = "localhost:8848" # nacos服务地址
       namespace = ""
       cluster = "default"
     }
   }
   config {
     type = "nacos" # file、nacos、apollo、zk、consul、etcd3
     nacos {
       serverAddr = "localhost:8848" # nacos服务地址
       namespace = ""
     }
   }
   ```
5. 启动：`seata-server.sh -h 127.0.0.1 -p 8091 -m db -n 1 -e test`<br />
   因为配置好了`registry.conf`，可以不用任何参数直接启动seata server。
   - `-h`：注册中心地址；
   - `-p`：Seata Server监听端口号；
   - `-m`：Seata事务信息存储模式；
   - `-n`：多个Seata Server组成集群时，通过该参数标识本次启动的节点，避免不同节点生成`transactionId`冲突；
   - `-e`：多环境配置，参考[Multi-configuration Isolation](http://seata.io/en-us/docs/ops/multi-configuration-isolation.html)；

----------------------------------------
#### Seata使用
在[my-demo](https://github.com/liuzhibin-cn/my-demo)项目进行功能演示，请先参考项目相关说明。下面演示AT事务模式的使用。

##### [shop-web](https://github.com/liuzhibin-cn/my-demo/tree/master/shop-web)：SpringBoot Web项目
`shop-web`没有操作数据库，只是引用Dubbo服务完成注册、登录、下单等演示功能。在这里开启全局事务。

项目之前已经使用nacos作为Dubbo的注册中心，通过`dubbo-registry-nacos`导入了nacos客户端依赖项，所以无需再为Seata导入nacos客户端。
1. [pom.xml](https://github.com/liuzhibin-cn/my-demo/blob/master/shop-web/pom.xml)添加依赖项：
   ```xml
   <dependency>
       <groupId>io.seata</groupId>
       <artifactId>seata-spring-boot-starter</artifactId>
       <version>1.0.0</version>
   </dependency>
   ```
2. [application.yml](https://github.com/liuzhibin-cn/my-demo/blob/master/shop-web/src/main/resources/application.yml)添加配置：
   ```yml
   seata:
     enabled: true
     application-id: ${application.name}
     # 必须与Server端的service.vgroup_mapping.my_demo_gtx保持一致
     tx-service-group: my_demo_gtx
     config:
       type: nacos
       nacos:
         namespace:
         serverAddr: 192.168.31.108:8848
     registry:
       type: nacos
       nacos:
         cluster: default
         server-addr: 192.168.31.108:8848
         namespace:
   ```
3. 在全局事务入口方法上添加注解，开启全局事务。例如[ShopController.java](https://github.com/liuzhibin-cn/my-demo/blob/master/shop-web/src/main/java/my/demo/test/ShopController.java#L110)：
   ```java
   @GlobalTransactional(timeoutMills = 3000, name = "full-test-case")
   ```

`shop-web`项目对Dubbo服务的调用无需任何其它处理，Seata自动完成，原理同APM，通过Dubbo filter传递全局事务信息。

##### [order-service](https://github.com/liuzhibin-cn/my-demo/tree/master/order-service)：Dubbo服务
Dubbo服务操作数据库完成业务处理，在这里只需要为Seata配置数据源拦截即可。

同上，在`pom.xml`添加依赖项，在`application.yml`添加配置，配置内容一样。

[SeataConfiguration.java](https://github.com/liuzhibin-cn/my-demo/blob/master/service-client/src/main/java/my/demo/utils/SeataConfiguration.java)配置：
```java
@Configuration
@ConditionalOnClass({ DataSourceProxy.class, SqlSessionFactory.class })
@AutoConfigureBefore(MybatisAutoConfiguration.class)
public class SeataConfiguration {
	Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Value("${mybatis.mapperLocations}")
	String mapperLocations;
	
	@Bean
    public DataSourceProxy dataSourceProxy(DruidDataSource druidDataSource){
        return new DataSourceProxy(druidDataSource);
    }
	//为Spring的DataSourceTransactionManager使用dataSourceProxy
    @Bean
    public DataSourceTransactionManager transactionManager(DataSourceProxy dataSourceProxy) {
        return new DataSourceTransactionManager(dataSourceProxy);
    }
    // 项目使用了mybatis，使用dataSourceProxy创建SqlSessionFactory bean
    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSourceProxy dataSourceProxy) throws Exception {
    	log.info("[seata-configuration] Mybatis sqlSessionFactory created, mapperLocations: " + mapperLocations);
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSourceProxy);
        factoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(mapperLocations));
        factoryBean.setTransactionFactory(new SpringManagedTransactionFactory());
        return factoryBean.getObject();
    }
}
```

AT事务模式下，需要在业务库中建立[undo_log](https://github.com/seata/seata/blob/develop/script/client/at/db/mysql.sql)表，用于Seata保存回滚日志。<br />
`my-demo`项目使用了Mycat演示分库分表，将`undo_log`建在`mydemo-dn1`中，Mycat的[schema.xml](https://github.com/liuzhibin-cn/my-demo/blob/master/docs/mycat-conf/schema.xml)中，在逻辑库`db_order`和`db_user`下面都添加了`undo_log`表：
```xml
<schema name="db_order" checkSQLschema="false" sqlMaxLimit="100">
	...
	<!-- Seata的回滚表，Seata要求回滚表在业务库中，因此必须添加到mycat逻辑库中 -->
	<table name="undo_log" dataNode="dn1" primaryKey="id" />
</schema>
<schema name="db_user" checkSQLschema="false" sqlMaxLimit="100">
	...
	<table name="undo_log" dataNode="dn1" primaryKey="id" />
</schema>

```

项目数据源使用Druid，配置参考[DruidConfiguration.java](https://github.com/liuzhibin-cn/my-demo/blob/master/service-client/src/main/java/my/demo/utils/DruidConfiguration.java)、[DruidProperties.java](https://github.com/liuzhibin-cn/my-demo/blob/master/service-client/src/main/java/my/demo/utils/DruidProperties.java)、[application.yml](https://github.com/liuzhibin-cn/my-demo/blob/master/order-service/src/main/resources/application.yml)。

为了保证本地事务完整性，[OrderServiceImpl.java](https://github.com/liuzhibin-cn/my-demo/blob/master/order-service/src/main/java/my/demo/service/order/OrderServiceImpl.java)中，`createOrder`方法加了`@Transactional`注解，使用了Spring事务。

为了简单了解Seata全局事务启动情况，代码中使用`log.info("[create] XID: " + RootContext.getXID());`打印全局事务ID。

[user-service](https://github.com/liuzhibin-cn/my-demo/tree/master/user-service)的配置同`order-service`。

下面是一段演示逻辑对应的SQL日志，关键词小写的SQL都是业务操作，大写的都是Seata操作：
```sql
-- 业务操作 1
insert into `ord_order_item` (order_id, item_id, title, quantity, price, subtotal, discount, created_at) values(?, ?, ?, ?, ?, ?, ?, ?)
-- Seata: 为 insert 操作生成 AfterImage，记录回滚日志
SELECT * FROM `ord_order_item` WHERE order_item_id in (?)
INSERT INTO `undo_log` (branch_id, xid, context, rollback_info, log_status, log_created, log_modified) VALUES (?, ?, ?, ?, ?, now(), now())
-- 业务操作 2
insert into `ord_order` (order_id, user_id, status, total, discount, payment, pay_time, pay_status, contact, phone, address, created_at) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
-- Seata: 为 insert 操作生成 AfterImage，记录回滚日志
SELECT * FROM `ord_order` WHERE order_id in (?)
INSERT INTO `undo_log` (branch_id, xid, context, rollback_info, log_status, log_created, log_modified) VALUES (?, ?, ?, ?, ?, now(), now())
-- 业务操作 3
insert into `ord_user_order` (user_id, order_id) values(?, ?)
-- Seata: 为 insert 操作生成 AfterImage，记录回滚日志
SELECT * FROM `ord_user_order` WHERE id in (?)
INSERT INTO `undo_log` (branch_id, xid, context, rollback_info, log_status, log_created, log_modified) VALUES (?, ?, ?, ?, ?, now(), now())
-- Seata: 为 update 操作生成 BeforeImage
SELECT `order_item_id`, subtotal, discount FROM `ord_order_item` WHERE order_id = ? FOR UPDATE
-- 业务操作 4
update `ord_order_item` set subtotal=subtotal-2, discount=discount+2 where order_id=?
-- Seata: 为 update 操作生成 AfterImage，记录回滚日志
SELECT `order_item_id`, subtotal, discount FROM ord_order_item WHERE order_item_id in (?,?)
INSERT INTO `undo_log` (branch_id, xid, context, rollback_info, log_status, log_created, log_modified) VALUES (?, ?, ?, ?, ?, now(), now())
-- Seata: 全局事务提交时，清除回滚日志
DELETE FROM undo_log WHERE  branch_id IN  (?,?,?,?,?)  AND xid IN  (?)
```

----------------------------------------
#### Seata与Mycat集成注意事项
- **Mycat全局序列不支持`next value for MYCATSEQ_XXX`用法** <br />
  因为Druid和Seata都要解析SQL语句，非标准SQL无法解析导致异常。<br />
  **解决方案**：<br />
  1. 使用Mycat的`autoIncrement`：<br />
     ```xml
     <table name="usr_user" primaryKey="user_id" autoIncrement="true" dataNode="dn1, dn2" rule="user-rule" />
     ```
     仅限于主键，非主键不支持使用`autoIncrement`。<br />
     与MySQL自增字段类似，`insert`语句无需指定`user_id`列。Mycat先使用全局序列获取`user_id`值，改写SQL，在`insert`语句中添加上`user_id`字段，这样随后可以使用`user_id`进行分片路由等。
  2. 用表名称`USR_USER`建立Mycat全局序列，参考[sequence_db_conf.properties](https://github.com/liuzhibin-cn/my-demo/blob/master/docs/mycat-conf/sequence_db_conf.properties)和[sql-schema.sql](https://github.com/liuzhibin-cn/my-demo/blob/master/docs/sql-schema.sql)。
- **主键使用Mycat全局序列，必须将MySQL字段设为自增类型** <br />
  按照上面方法将主键配置为使用Mycat全局序列，Mycat是可以正常工作了，但是还不支持`last_insert_id()`函数。<br />
  Seata依赖主键值进行回滚补偿、实现行级全局锁，它拦截SQL语句，从数据库查询主键字段，发现主键`user_id`并没有出现在`insert`语句中，并且不是MySQL自增字段，因此无法继续，抛出异常。<br />
  **解决方案**：在MySQL中将字段设置为自增类型。这样设置后，执行完`insert`语句，然后`last_insert_id()`返回的就是Mycat全局序列生成的`user_id`值，这可以用于MyBatis的`selectKey`中。
- **Seata不支持组合主键** <br />
  目前`Seata 1.0.0`版本不支持组合主键，仅支持单个字段的主键。<br />
  **解决方案**：添加一个无业务语义的ID字段作为主键即可。组合主键在多对多关联关系表中比较常见，另外没有主键的表Seata也不支持，同样需要添加一个无业务含义的主键字段。
- **分库分表后Seata会导致Mycat产生较多的跨分片查询** <br />
  生成回滚日志中的`AfterImage`是通过主键查询数据的；回滚操作通过主键更新数据。如果主键不是分片字段，这些语句将路由到全部分片中执行。<br />
  `my-demo`项目中，下面几个表主键不是分片字段：
  - `ord_order_item`：与`ord_order`为父子表，随父表`order_id`分片；
  - `ord_user_order`：多对多关联表，专为订单分库分表而设计的索引表，用于改善查询用户订单列表，因Seata不支持组合主键而增加无业务意义主键，不可能使用主键分片；
  - `usr_user_account`：设计问题，用户登录时根据账号查数据，但账号是字符串不方便直接用于分片，因此额外添加`account_hash`作为分片字段，这种情况可以采用自定义分片函数避免；

----------------------------------------
#### Seata与Sharding-Proxy集成注意事项
- **`Sharding-Proxy`必须设置默认数据源`defaultDataSourceName`**。<br />
  `Seata`生成回滚日志时需要获取数据库元数据信息，会使用`SHOW FULL COLUMNS FROM usr_user LIKE '%'`，`Sharding-Proxy`没有设置默认数据源时会报错。<br />
  ```yml
  defaultDataSourceName: ds_0
  ```
- **只能通过MySQL的`GENERATED_KEY`获取`keyGenerator`生成的值，不能使用`last_insert_id()`函数** <br />
  MyBatis中的使用方法：
  ```java
  @Insert("insert into usr_user (nickname, mobile, email, created_at) values (#{nickname}, #{mobile}, #{email}, #{createdAt})")
  //用 Options 代替 SelectKey
  @Options(useGeneratedKeys=true, keyProperty="userId", keyColumn="user_id")
  int createUser(User user);
  ```