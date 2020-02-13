CREATE USER mydemo IDENTIFIED BY 'mydemo';
GRANT ALL ON `mydemo-dn0`.* TO mydemo@'%';
GRANT ALL ON `mydemo-dn1`.* TO mydemo@'%';
GRANT ALL ON `mydemo-dn2`.* TO mydemo@'%';
GRANT ALL ON `mydemo-dn3`.* TO mydemo@'%';
GRANT ALL ON `mydemo-dn4`.* TO mydemo@'%';

CREATE USER nacos IDENTIFIED BY 'nacos';
GRANT ALL ON `nacos`.* TO nacos@'%';

CREATE USER zipkin IDENTIFIED BY 'zipkin';
GRANT ALL ON `zipkin`.* TO zipkin@'%';

CREATE USER skywalking IDENTIFIED BY 'skywalking';
GRANT ALL ON `skywalking`.* TO skywalking@'%';

CREATE USER seata IDENTIFIED BY 'seata';
GRANT ALL ON `seata`.* TO seata@'%';

-- docker的mysql:5.7.18镜像为root@'%'用户分配的权限有限，无法远程连接
GRANT ALL ON *.* TO root@'%';

FLUSH PRIVILEGES;