#!/usr/bin/env sh

PROJECT_HOME=`dirname "$0"`

show_usage() {
	echo " Run \"mvn clean package spring-boot:repackage -P profiles\" for all services and shop-app application."
	echo " Usage:"
	echo " 1. Options to enable database sharding:"
	echo "    -mycat"
	echo "    -sharding-proxy"
	echo " 2. Options to enable global transaction management:"
	echo "   -seata"
	echo " 3. Options to enable APM tools:"
	echo "   -skywalking"
	echo "   -pinpoint"
	echo "   -zipkin"
}
package_project() {
	cd $1
	mvn $CLEAN package spring-boot:repackage "$PROFILES"
	cd ..
}

if [ $# -eq 0 ]; then
	show_usage
	sleep 1
fi;

PROFILES="-P dev"
DB_HOST="mysql"
APM=""
CLEAN=""

while [ -n "$1" ] 
do
	case "$1" in 
		-mycat|--mycat) 
		    PROFILES="$PROFILES,mycat";
		    DB_HOST="mycat"
		    shift 1;;
		-shardingproxy|--shardingproxy) 
		    PROFILES="$PROFILES,shardingproxy"; 
		    DB_HOST="shardingproxy"
		    shift 1;;
		-seata|--seata) PROFILES="$PROFILES,seata"; shift 1;;
		-zipkin|--zipkin) 
			PROFILES="$PROFILES,zipkin"; 
			APM="zipkin"
			shift 1;;
		-skywalking|--skywalking) 
			PROFILES="$PROFILES,skywalking"; 
			APM="skywalking"
			shift 1;;
		-pinpoint|--pinpoint) 
			PROFILES="$PROFILES,pinpoint"; 
			APM="pinpoint"
			shift 1;;
		-clean|--clean) CLEAN="clean"; shift 1;;
		?|-?|-help|--help) show_usage; exit 0;;
		--) break;;
		*) echo " Invalid parameter: $1"; exit 1;;
	esac
done

cd $PROJECT_HOME
mvn install
cd service-client
mvn clean install
cd ..

package_project "item-service"
package_project "stock-service"
package_project "user-service"
package_project "order-service"
package_project "shop-web"

SED="sed"
if [ `uname` == "Darwin" ]; then
    SED="gsed"
fi
# order和user服务需要连接数据库：
# 1. 本地运行模式：通过maven的profile完成数据库HOST、PORT配置；
# 2. Docker容器运行：通过docker run传递环境变量，将数据库HOST、PORT传递到容器；
$SED -i "s/^MYSQL_HOST=.*$/MYSQL_HOST=$DB_HOST/g" order-service/run.sh
$SED -i "s/^MYSQL_HOST=.*$/MYSQL_HOST=$DB_HOST/g" user-service/run.sh
# 3. K8s运行：修改通过yaml文件指定HOST、PORT
$SED -i "s/(db|mycat)-mydemo/$DB_HOST/g" k8s/svc-user-deployment.yaml
$SED -i "s/(db|mycat)-mydemo/$DB_HOST/g" k8s/svc-order-deployment.yaml
# APM使用zipkin和skywalking时，Docker容器构建方法不一样，这里为Docker容器构建进行参数设置
$SED -i "s/^APM=.*$/APM=$APM/g" item-service/build.sh
$SED -i "s/^APM=.*$/APM=$APM/g" stock-service/build.sh
$SED -i "s/^APM=.*$/APM=$APM/g" user-service/build.sh
$SED -i "s/^APM=.*$/APM=$APM/g" order-service/build.sh
$SED -i "s/^APM=.*$/APM=$APM/g" shop-web/build.sh

echo " Finished"
