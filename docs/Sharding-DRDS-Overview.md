本文主要参考DRDS文档整理而来。

DRDS将TDDL和Cobar整合起来，从2012年开始发展到现在，功能已经非常成熟。除了将后端数据库聚合成一个逻辑库，提供数据垂直拆分、水平拆分、读写分离等功能外，还具备功能比较齐全的分布式SQL查询、优化、执行引擎，提供分布式事务管理方案，等等，DRDS已经不是一个简单的数据库代理、分库分表中间件，已经发展为分布式数据库引擎。将后端数据库看作一个高级版存储引擎，DRDS则可以看作数据库的执行引擎，DRDS集群和后端数据库集群共同组成一个分布式数据库。

#### DRDS架构
参考[DRDS产品架构](https://help.aliyun.com/document_detail/117771.html)、[DRDS扩展性原理](DRDS扩展性原理)：<br />
<img src="https://richie-leo.github.io/ydres/img/10/120/1014/DRDS-architecture.jpg" style="width:99%;max-width:900px;" />

- 后端使用RDS实例（即MySQL、MariaDB等数据库），中间为DRDS实例。DRDS无状态，多实例负载均衡；
- 面向应用端，DRDS实现了MySQL协议，对客户端应用透明，支持任何语言；<br />
  <img src="https://richie-leo.github.io/ydres/img/10/120/1014/DRDS-concept-model.jpg" style="width:99%;max-width:550px;" />
- DRDS与RDS之间采用TDDL（客户端分库分表组件），避免了应用中直接使用TDDL的复杂性、语言限制等缺点；
- DRDS内核架构：<br />
  <img src="https://richie-leo.github.io/ydres/img/10/120/1014/DRDS-kernal-architecture.jpg" style="width:99%;max-width:1000px;" />

#### 执行引擎
参考[SQL调优基础概念](https://help.aliyun.com/document_detail/144289.html)、[查询优化器介绍](https://help.aliyun.com/document_detail/144292.html)、[查询执行器介绍](https://help.aliyun.com/document_detail/144294.html)、[执行计划和基本算子](https://help.aliyun.com/document_detail/144296.html)、[Join与子查询的优化和执行](https://help.aliyun.com/document_detail/144297.html)等。

结合后端数据库，DRDS形成了一个比较完整的查询执行引擎，某些方面比MySQL方案更多：
- 支持并行查询；<br />
  例如同一个SQL，未使用并行（`concurrent`是并发而非并行，指多线程执行分片查询，将结果集汇总）：
  ```js
  HashAgg(group="l_orderkey", revenue="SUM(*)")
    HashJoin(condition="o_custkey = c_custkey", type="inner")
      Gather(concurrent=true)
        LogicalView(tables="ORDERS_[0-7],LINEITEM_[0-7]", shardCount=8, sql="SELECT `ORDERS`.`o_custkey`, `LINEITEM`.`l_orderkey`, (`LINEITEM`.`l_extendedprice` * (? - `LINEITEM`.`l_discount`)) AS `x` FROM `ORDERS` AS `ORDERS` INNER JOIN `LINEITEM` AS `LINEITEM` ON (((`ORDERS`.`o_orderkey` = `LINEITEM`.`l_orderkey`) AND (`ORDERS`.`o_orderdate` < ?)) AND (`LINEITEM`.`l_shipdate` > ?))")
      Gather(concurrent=true)
      LogicalView(tables="CUSTOMER_[0-7]", shardCount=8, sql="SELECT `c_custkey` FROM `CUSTOMER` AS `CUSTOMER` WHERE (`c_mktsegment` = ?)")
  ```
  使用并行：
  ```js
  Gather(parallel=true)
    ParallelHashAgg(group="o_orderdate,o_shippriority,l_orderkey", revenue="SUM(*)")
      ParallelHashJoin(condition="o_custkey = c_custkey", type="inner")
        LogicalView(tables="ORDERS_[0-7],LINEITEM_[0-7]", shardCount=8, sql="SELECT `ORDERS`.`o_custkey`, `ORDERS`.`o_orderdate`, `ORDERS`.`o_shippriority`, `LINEITEM`.`l_orderkey`, (`LINEITEM`.`l_extendedprice` * (? - `LINEITEM`.`l_discount`)) AS `x` FROM `ORDERS` AS `ORDERS` INNER JOIN `LINEITEM` AS `LINEITEM` ON (((`ORDERS`.`o_orderkey` = `LINEITEM`.`l_orderkey`) AND (`ORDERS`.`o_orderdate` < ?)) AND (`LINEITEM`.`l_shipdate` > ?))", parallel=true)
        LogicalView(tables="CUSTOMER_[0-7]", shardCount=8, sql="SELECT `c_custkey` FROM `CUSTOMER` AS `CUSTOMER` WHERE (`c_mktsegment` = ?)", parallel=true)
  ```
  使用并行查询，Plan中的算子不一样了。并行算子先对输入进行分区（并不是分片返回的结果集），然后每个分区启动一个线程执行算子操作，最后将多份结果合并，与Map-Reduce大体类似，同属并行计算。
- 关联算法：目前支持Nested-Loop Join、Hash Join、Sort-Merge Join、Lookup Join（BKAJoin）。<br />
  > 对比一下：MySQL经过了这么多年发展，到目前的MySQL 8版本中，仍然只实现了2种Nested-Loop关联算法，Hash Join、Sort-Merge Join在Oracle、SQL Server、PostgreSQL都有实现。
- 排序算法：MemSort、TopN；
- 聚合算法：HashAgg、SortAgg；

##### 查询执行过程
DRDS SQL执行过程：<br />
<img src="https://richie-leo.github.io/ydres/img/10/120/1014/query-execution.png" style="width:99%;max-width:750px;" />

查询优化器：
- RBO：子查询去关联化、算子下推等；
- CBO：维护统计信息，基于统计信息的Cardinality Estimation，实现成本评估模型，选择成本较优的执行计划；

执行计划缓存：<br />
<img src="https://richie-leo.github.io/ydres/img/10/120/1014/query-plan-cache.png" style="width:99%;max-width:700px;" />

##### Volcano执行模型
所有算子都定义了`open()`、`next()`等接口，算子根据执行计划组合成一棵算子树，上层算子通过调用下层算子的`next()`接口的取出结果，完成该算子的计算。最终顶层算子产生用户需要的结果并返回给客户端。<br />
<img src="https://richie-leo.github.io/ydres/img/10/120/1014/volcano-model.png" style="width:99%;max-width:750px;" />

##### 算子下推
查询优化最有效的措施是尽可能将操作下推给后端数据库，避免DRDS内的操作。一些简单、可以落在单库执行的SQL，可以整条语句下推给后端数据库；复杂查询则尽可能多的将子操作-算子下推，不能下推的部分由DRDS执行器处理：<br />
<img src="https://richie-leo.github.io/ydres/img/10/120/1014/sql-engine-1.png" style="width:99%;max-width:600px;" />

示例SQL：
```sql
SELECT l_orderkey, sum(l_extendedprice *(1 - l_discount)) AS revenue
FROM CUSTOMER, ORDERS, LINEITEM
WHERE c_mktsegment = 'AUTOMOBILE' and c_custkey = o_custkey and l_orderkey = o_orderkey 
  and o_orderdate < '1995-03-13' and l_shipdate > '1995-03-13'
GROUP BY l_orderkey;
```

`EXPLAIN`展示的DRDS执行计划：
```js
HashAgg(group="l_orderkey", revenue="SUM(*)")
  HashJoin(condition="o_custkey = c_custkey", type="inner")
    Gather(concurrent=true)
      LogicalView(tables="ORDERS_[0-7],LINEITEM_[0-7]", shardCount=8, sql="SELECT `ORDERS`.`o_custkey`, `LINEITEM`.`l_orderkey`, (`LINEITEM`.`l_extendedprice` * (? - `LINEITEM`.`l_discount`)) AS `x` FROM `ORDERS` AS `ORDERS` INNER JOIN `LINEITEM` AS `LINEITEM` ON (((`ORDERS`.`o_orderkey` = `LINEITEM`.`l_orderkey`) AND (`ORDERS`.`o_orderdate` < ?)) AND (`LINEITEM`.`l_shipdate` > ?))")
    Gather(concurrent=true)
      LogicalView(tables="CUSTOMER_[0-7]", shardCount=8, sql="SELECT `c_custkey` FROM `CUSTOMER` AS `CUSTOMER` WHERE (`c_mktsegment` = ?)")
```

<img src="https://richie-leo.github.io/ydres/img/10/120/1014/operation-pushdown.png" style="width:99%;max-width:660px;" />

- `LogicalView`都是下推给后端数据库的操作；
- `ORDERS`和`LINEITEM`为父子关系表，因此可以将这2个表的关联查询下推给各个分片；
- `CUSTOMER`与`ORDERS`的关联无法由数据库完成，由DRDS汇总（`Gather`）2个结果集后在内存中执行`HashJoin`；
- 最后使用`HashAgg`进行聚合计算；

#### 全局二级索引 GSI
概念参考[DRDS全局二级索引](https://help.aliyun.com/document_detail/142733.html)。在[Mycat分库分表概览](https://github.com/liuzhibin-cn/my-demo/blob/master/docs/Sharding-Mycat-Overview-Quickstart.md)中也创建了自定义索引表，在应用代码中维护。DRDS支持全局二级索引GSI，自动维护：<br />
<img src="https://richie-leo.github.io/ydres/img/10/120/1014/DRDS-GSI.png" style="width:99%;max-width:700px;" />

数据写入时通过XA多写维护GSI，并确保数据强一致性。回表：即数据库的RID lookup。

DRDS扩展了MySQL DDL语法来管理GSI，参考[DRDS全局二级索引使用文档](https://help.aliyun.com/document_detail/142739.html)，支持索引类型BTREE、HASH。

#### 弹性计算
- DRDS无状态，方便弹性扩容。面向OLTP业务，除分片路由、读写分离路由等基础功能外，还需要处理跨分片查询，包括结果集合并（合并过程需要处理GROUP BY再聚合、ORDER BY重排序等）、分页处理（SQL改写、结果集合并分页）等OLTP的基础必要功能；
- 面向OLAP、低频的复杂查询等，可能设计跨分片关联数据量过大，则通过架构图中的Fireworks（DAG，多机并行处理）部分进行分布式内存计算，支持弹性扩容；<br />
  <img src="https://richie-leo.github.io/ydres/img/10/120/1014/DRDS-DAG.jpg" style="width:99%;max-width:600px;" />

DRDS支持的平滑扩容实现方式：在同一个后端RDS实例上创建多个MySQL数据库（默认8个），RDS实例负载、存储空间过高时，申请新的RDS实例，将部分MySQL数据库迁移到新的RDS实例上，参考[DRDS 平滑扩容](https://help.aliyun.com/document_detail/52132.html)。

#### 分布式事务
参考[DRDS分布式事务](https://help.aliyun.com/document_detail/71230.html)：
- 2PC事务：DRDS实现的弱一致性两阶段事务方案，用于后端数据库不支持XA事务或支持不完善的场景，例如MySQL 5.7以下版本；
  > PREPARE阶段将用户SQL记录到REDO_LOG表，COMMIT阶段若发生异常，尝试使用REDO_LOG重做。没有UNDO机制，REDO时不能确保期间不出现脏写。注意这是DRDS自己实现的2PC逻辑，跟数据库XA里面的2PC没有关系。
- XA强一致性事务：MySQL 5.7开始XA事务比较成熟，DRDS直接使用MySQL XA事务，并且默认全局开启；
- Flexible柔性事务：XA事务提供强一致性，柔性事务提供最终一致性，类似于悲观锁和乐观锁的区别；
  > 柔性事务记录数据变更前的IMAGE，分布式事务失败时可以根据SQL语义和IMAGE自动生成回滚补偿SQL，撤销已经执行成功的业务操作，即将TCC中的Cancel由分布式事务管理器自动实现，无需业务代码参与。

阿里分布式事务发展情况：
- 淘宝、天猫早期大量使用事务消息（RocketMQ）实现分布式事务，而支付宝早期采用XTS（TCC事务模型）实现分布式事务；
  > 事务消息、XTS都要求业务代码实现大量事务控制逻辑（事务状态回查、事务补偿，以及TCC中的Try-Confirm等），对一线开发人员要求高，容易造成数据不一致问题。
- 基于阿里大量使用TDDL、DRDS场景下分布式事务要求，开发了TXC系统（柔性事务），通过TC记录Undo Log实现自动回滚，无需业务代码实现，将分布式事务管理从应用中隔离出来。
- 2017年阿里云上线全局事务服务[GTS](https://help.aliyun.com/product/48444.html)产品，发展越来越成熟，提供跨数据库/MQ、跨服务的分布式事务处理能力，参考[GTS产品概述](https://help.aliyun.com/document_detail/48726.html)：
  - 多数据源支持：DRDDS、RDS、MySQL、Oracle、PostgreSQL、OceanBase等；
  - 跨服务的分布式事务管理，支持Dubbo、SpringBoot事务控制、SpringCloud；
  - 事务消息支持；
  - 灵活的事务模式：参考[GTS事务模式简介
](https://help.aliyun.com/document_detail/53296.html)
    - AT模式对应用几乎零侵入；
    - MT模式对应TCC事务模型，满足特殊要求，例如与未使用GTS的其它应用和服务对接等；
- 2019年GTS的开源版[Seata](http://seata.io/zh-cn/)发布，不仅在阿里云，企业内部也可以用上GTS了；

DRDS的分布式事务与[GTS](https://help.aliyun.com/product/48444.html)的关系和区别：
- DRDS自己有一套分布式事务管理系统，对集群内后端数据库的分布式事务进行管理，场景上比GTS简单，因为简化来看TM只有DRDS自己的实例；
- GTS是DRDS外面的一层分布式事务管理方案，如果存储用的DRDS，使用GTS的AT事务模式时，GTS直接使用DRDS的分布式事务管理，对于其它跨库、跨服务的分布式事务，GTS才采用自己的分布式事务管理方案；

#### 全局序列 Sequence
DRDS全局唯一数字序列，参考[DRDS Sequence介绍](https://help.aliyun.com/document_detail/71261.html)，使用限制参考[Sequence限制及注意事项](https://help.aliyun.com/document_detail/71255.html)。

两类用法：
- 显示Sequence：通过DRDS扩展的Sequence DDL语法创建和维护，通过`select seq.nextval`取值；
- 隐式Sequence：建表时指定`AUTO_INCREMENT`，DRDS将自动维护全局Sequence；

DRDS主要是利用MySQL全局表来生成全局序列，Sequence类型：
- Group Sequence：将号段分组，不同分组可以放在不同MySQL实例中，提高性能、吞吐率和可用性，但Sequence值不能单调递增，可能产生跳跃段；
- 单元化Group Sequence：在Group Sequence基础上提供单元化能力。<br />
  > 官方文档：单元化指能够跨实例、跨库分配全局唯一序列。<br />
  > 单元化指的是多地多机房、异地多活的部署方案（不是异地灾备），所以这里的跨实例、跨库，指的是跨DRDS逻辑库、逻辑实例，而DRDS逻辑实例由多个DRDS实例的集群组成。所以单元化Group Sequence需要指定单元数量、单元索引，且不同机房中的单元数量必须相同。
- Simple Sequence：只使用一个MySQL表，并且号段不分组，性能最差，但保证连续、单调递增；
- Time-based Sequence：不依赖数据库，DRDS内存中生成，基于时间戳 + 节点编号 + 序列号组合而成，性能好；

Group Sequence原理：<br />
假设sequence名称为my_seq，共3个分组，内步长1000，初始值如下：
```text
+---------------+------------+---------------+
| sequence_name | group_name | current_value |
+---------------+------------+---------------+
| my_seq        | group_0    |             0 |
| my_seq        | group_1    |          1000 |
| my_seq        | group_2    |          2000 |
+---------------+------------+---------------+
```

某节点需要分配下一个sequence值时：
1. 随机从3个分组里面取一个，假设取到`group_1`，则得到sequence当前值1000；<br />
2. 将`group_1`的当前值更新：<br />
   内步长1000表示该节点一次性将号段`(1000, 2000]`取走，因为设置了3个分组，`外步长=内步长*3`，所以将sequence当前值更新为4000：
   ```text
   +---------------+------------+---------------+
   | sequence_name | group_name | current_value |
   +---------------+------------+---------------+
   | my_seq        | group_0    |             0 |
   | my_seq        | group_1    |          4000 |
   | my_seq        | group_2    |          2000 |
   +---------------+------------+---------------+
   ```
3. 节点将sequence当前值缓存在内存中，每次客户端请求下一个sequence值，单调递增分配下去。内步长为1000表示该节点本次申请到的号段为`(1000, 2000]`，当前sequence值到达2000后，重新按步骤申请下一个号段；
   > 节点中途停机退出，已经申请的号段中没有使用的值被丢弃，不再使用。