#### 运行容器
docker network create mydemo

docker run -d --net=mydemo --name mysql -p13306:3306 -e MYSQL_ROOT_PASSWORD=123 mydemo/mysql

docker run -d --net=mydemo --name nacos -p18848:8848 -e MYSQL_HOST=mysql mydemo/nacos

docker run -d --net=mydemo --name item -e NACOS_HOST=nacos mydemo/item
docker run -d --net=mydemo --name stock -e NACOS_HOST=nacos mydemo/stock
docker run -d --net=mydemo --name user -e MYSQL_HOST=mysql -e NACOS_HOST=nacos mydemo/user
docker run -d --net=mydemo --name order -e MYSQL_HOST=mysql -e NACOS_HOST=nacos mydemo/order
docker run -d --net=mydemo --name shopweb -p18090:8090 -e NACOS_HOST=nacos mydemo/shopweb