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

GRANT ALL ON *.* TO root@'%';

FLUSH PRIVILEGES;