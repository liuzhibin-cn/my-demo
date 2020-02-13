#!/bin/sh

# 要求本地安装以下依赖项：
# 1. docker client；
# 2. git client；
# 3. java8、maven；

# ========================================================================================
# 构建nacos镜像
NACOS_VERSION="1.1.4"

if [ ! -d "./dist" ]; then
	# 在本地下载源码编译
	mkdir src
	cd src
	# getee.com为一些优秀的github项目建立了镜像，从这里下载比github快
	git clone -b $NACOS_VERSION --depth=1 https://gitee.com/mirrors/Nacos.git .
	mvn -Prelease-nacos clean install -U -DskipTests=true
	mv distribution/target/nacos-server-$NACOS_VERSION.tar.gz ../
	cd ..
	tar xzf nacos-server-$NACOS_VERSION.tar.gz -C ./
	mv nacos dist
	rm -rf src # nacos-server-$NACOS_VERSION.tar.gz
fi

# 将后台启动nacos改为前台启动
sed -i '/echo "$JAVA ${JAVA_OPT}"/'d dist/bin/startup.sh
sed -i 's/nohup.*/$JAVA ${JAVA_OPT} nacos.nacos/g' dist/bin/startup.sh
sed -i '/echo "nacos is starting，you can check the/'d dist/bin/startup.sh
# 将standalone模式内存参数改小些
sed -i 's/-Xms512m -Xmx512m -Xmn256m/-Xms128m -Xmx512m -Xmn32m/g' dist/bin/startup.sh
# 配置nacos使用容器环境变量
cp dist/conf/application.properties.example dist/conf/application.properties
sed -i 's/db.num=2/db.num=1/g' dist/conf/application.properties
sed -i 's/db.url.0=jdbc:mysql:\/\/11.162.196.16:3306\/nacos_devtest/db.url.0=jdbc:mysql:\/\/${MYSQL_HOST:127.0.0.1}:3306\/nacos/g' dist/conf/application.properties
sed -i '/db.url.1/'d dist/conf/application.properties
sed -i 's/db.user=nacos_devtest/db.user=${MYSQL_USER:nacos}/g' dist/conf/application.properties
sed -i 's/db.password=nacos/db.password=${MYSQL_PSW:nacos}/g' dist/conf/application.properties

docker build -t mydemo/nacos .
# rm -rf dist