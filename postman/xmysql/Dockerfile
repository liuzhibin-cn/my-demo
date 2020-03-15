FROM mydemo/nodejs:10

RUN	cnpm install -g xmysql

ENV MYSQL_HOST=mysql MYSQL_PORT=3306 MYSQL_USER=mydemo MYSQL_PSW=mydemo DB=mydemo-dn1
ADD entrypoint.sh /home/
ENTRYPOINT [ "sh", "-c", "/home/entrypoint.sh" ]