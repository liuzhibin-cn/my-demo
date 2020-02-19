#### Run the demo application in Docker container
Prerequisites:
1. OS: Linux, Mac or Windows (with a bash shell, such as git bash);
2. JDK 8 and apache maven;
3. Docker;

##### `init.sh`
```sh
$PROJECT_HOME/docker/init.sh
```
Do initializations before you run the demo application first time:
1. Create a docker network.
2. Build necessary docker images and run containers: MySQL, Mycat, ShardingProxy, Nacos, Seata, SkyWalking, ZipKin. <br />
   Taking `init.sh` as reference, build and run containers as you want.

##### `package.sh`
```sh
$PROJECT_HOME/package.sh -clean -mycat -seata -zipkin
```
Compile and package demo application with variant components support.

##### `app-container.sh`
Build docker images and run containers for all Dubbo services and shop-web in this demo application.
```sh
# Build images, then run containers.
$PROJECT_HOME/docker/app-container.sh -build -run
# Stop and remove containers, remove images, so as to repackage and rerun the demo application.
$PROJECT_HOME/docker/app-container.sh -stop -rm -rmi
```