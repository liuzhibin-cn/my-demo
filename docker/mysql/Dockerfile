FROM mysql:5.7.18

ENV LANG=C.UTF-8
# Database Initialization: Put the *.sql、*.sh scrips into /docker-entrypoint-initdb.d/ directory
# 1-app-schema.sql：Schemas for demo application
# 2-nacos-mysql.sql: Schemas for Nacos
# 9-users.sql：Users and privileges required by demo application middlewares
ADD ./scripts/*.sql /docker-entrypoint-initdb.d/
# Location of MySQL configuration files:
#   /etc/my.cnf
#   /etc/mysql/conf.d/*.cnf
#   /etc/mysql/mysql.conf.d/*.cnf
COPY ./scripts/my.cnf /etc/mysql/mysql.conf.d/