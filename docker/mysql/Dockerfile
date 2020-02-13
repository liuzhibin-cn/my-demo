FROM mysql:5.7.18

ENV LANG=C.UTF-8
# 数据库初始化：将初始化用的*.sql、*.sh放入/docker-entrypoint-initdb.d/目录下即可
# 1-app-schema.sql：演示应用和服务所需表结构
# 2-nacos-mysql.sql: nacos表结构
# 9-users.sql：默认mysql镜像的root@'%'权限设置不足，无法远程连接，这里为其添加权限
ADD *.sql /docker-entrypoint-initdb.d/
# 添加mysql server配置
# 默认配置文件位于:
#   /etc/my.cnf
#   /etc/mysql/conf.d/*.cnf
#   /etc/mysql/mysql.conf.d/*.cnf
COPY ./my.cnf /etc/mysql/mysql.conf.d/