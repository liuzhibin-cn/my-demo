CREATE USER mydemo IDENTIFIED BY 'mydemo';
GRANT ALL ON `mydemo-dn0`.* TO mydemo@'%';
GRANT ALL ON `mydemo-dn1`.* TO mydemo@'%';
GRANT ALL ON `mydemo-dn2`.* TO mydemo@'%';
GRANT ALL ON `mydemo-dn3`.* TO mydemo@'%';
GRANT ALL ON `mydemo-dn4`.* TO mydemo@'%';

CREATE USER nacos IDENTIFIED BY 'nacos';
GRANT ALL ON `nacos`.* TO nacos@'%';

-- docker的mysql:5.7.18镜像为root@'%'用户分配的权限有限，无法远程连接
GRANT ALL ON *.* TO root@'%';

FLUSH PRIVILEGES;