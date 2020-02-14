#### Docker容器管理
环境要求：
1. Mac、Windows环境都可以，但Windows环境必须支持bash，例如安装git bash，将相关bash工具路径添加到PATH中；
2. JDK 8 + maven；
3. Git客户端；
4. Docker；

```sh
# 创建Docker Network，初始化执行一次即可
docker network create mydemo

# 运行MySQL容器，已经建立了项目所需的全部数据库、表结构和用户
$PROJECT_HOME/docker/mysql/build.sh # 构建Image
$PROJECT_HOME/docker/mysql/run.sh   # 运行容器

# 运行Mycat容器
$PROJECT_HOME/docker/mycat/build.sh # 构建Image
$PROJECT_HOME/docker/mycat/run.sh   # 运行容器

# 运行Nacos容器
$PROJECT_HOME/docker/nacos/build.sh # 构建Image
$PROJECT_HOME/docker/nacos/run.sh   # 运行容器

# 运行Seata容器
$PROJECT_HOME/docker/seata/build.sh # 构建Image
$PROJECT_HOME/docker/seata/run.sh   # 运行容器

# 编译打包演示应用
$PROJECT_HOME/package.sh -mycat -seata -zipkin -clean
# 容器运行所有Dubbo服务和演示应用
$PROJECT_HOME/docker/mydemo.sh -build -run
```