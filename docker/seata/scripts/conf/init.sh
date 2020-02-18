#!/usr/bin/env bash

cd $(dirname "$0")

echo ">>> To check Nacos status"
# nc, nmap, telnet not installed in this image, use curl instead
VALUE=$(curl -s --connect-timeout 1 -X GET "http://$NACOS_HOST:$NACOS_PORT/nacos/v1/cs/configs?dataId=service.vgroup_mapping.my_demo_gtx&group=SEATA_GROUP")
if [ $? -ne 0 ]; then
    echo "  Error: Nacos not started properly!"
	exit 1
fi
if [[ "$VALUE" != "" && "$VALUE" != "default" && "$VALUE" != "config data not exist" ]]; then
    echo "  Error: Get config from Nacos returned error message: $VALUE"
	exit 1
fi
echo "  Nacos is running on $NACOS_HOST:$NACOS_PORT"

# Config Seata
sed -i "s/serverAddr.*/serverAddr=\"$NACOS_HOST:$NACOS_PORT\"/g" registry.conf
echo "Config: store.db.user=$MYSQL_USER"
sed -i "s/store.db.user.*/store.db.user=$MYSQL_USER/g" config.txt
sed -i "s/store.db.password.*/store.db.password=$MYSQL_PSW/g" config.txt
echo "Config: store.db.url=jdbc:mysql://$MYSQL_HOST:$MYSQL_PORT/$MYSQL_DB?useUnicode=true"
sed -i "s/store.db.url.*/store.db.url=jdbc:mysql:\/\/$MYSQL_HOST:$MYSQL_PORT\/$MYSQL_DB?useUnicode=true/g" config.txt
echo "Config: service.default.grouplist=$SEATA_HOST:$SEATA_PORT"
sed -i "s/service.default.grouplist.*/service.default.grouplist=$SEATA_HOST:$SEATA_PORT/g" config.txt

echo ">>> To import Seata configs into Nacos"
./nacos-config.sh -h $NACOS_HOST -p $NACOS_PORT
