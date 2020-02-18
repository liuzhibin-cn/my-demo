#### Docker容器管理
本地环境要求：
1. Mac、Windows环境都可以，但Windows环境必须支持bash，例如安装git bash，将相关bash工具路径添加到PATH中；
2. JDK 8 + maven；
3. Docker；

```sh
# init.sh:
# 首次运行使用。初始化Docker容器，为数据库和所有中间件构建Docker镜像，运行Docker容器。
# 如果不需要某个中间件，修改init.sh注释掉再执行。
$PROJECT_HOME/docker/init.sh

# 编译打包演示应用：启用Mycat、Seata和ZipKin
$PROJECT_HOME/package.sh -clean -mycat -seata -zipkin

# app-container.sh:
# 管理mydemo项目下所有Dubbo服务和shop-web应用的镜像和容器。
# 为Dubbo服务、shop-web应用构建镜像，运行容器：
$PROJECT_HOME/docker/app-container.sh -build -run
# 为Dubbo服务、shop-web应用停止容器，并删除容器和镜像
$PROJECT_HOME/docker/app-container.sh -stop -rm -rmi
```