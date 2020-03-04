#!/bin/sh

show_usage() {
	echo " Usage:"
	echo "   -build: Build images for item, stock, user, order services and shop-web app."
	echo "   -run: Run containers for item, stock, user, order services and shop-web app."
	echo "   -stop: Stop containers for item, stock, user, order services and shop-web app."
	echo "   -rm: Remove containers for item, stock, user, order services and shop-web app."
	echo "   -rmi: Remove images for item, stock, user, order services and shop-web app."
}

if [ $# -eq 0 ]; then
	show_usage
	exit 0
fi;

DOCKER=`dirname "$0"`
cd $DOCKER
DOCKER=`pwd`
cd $DOCKER/../
PROJECT_HOME=`pwd`

while [ -n "$1" ] 
do
	case "$1" in 
		-build|--build) 
			$PROJECT_HOME/item-service/build.sh;
			$PROJECT_HOME/stock-service/build.sh;
			$PROJECT_HOME/user-service/build.sh;
			$PROJECT_HOME/order-service/build.sh;
			$PROJECT_HOME/shop-web/build.sh;
			shift 1;;
		-run|--run) 
			$PROJECT_HOME/item-service/run.sh;
			$PROJECT_HOME/stock-service/run.sh;
			$PROJECT_HOME/user-service/run.sh;
			$PROJECT_HOME/order-service/run.sh;
			$PROJECT_HOME/shop-web/run.sh;
			shift 1;;
		-start|--start) 
			docker start item stock user order shopweb;
			shift 1;;
		-stop|--stop) 
			docker stop shopweb order user stock item;
			shift 1;;
		-rmi|--rmi) 
			docker rmi mydemo/shopweb mydemo/order mydemo/user mydemo/stock mydemo/item;
			shift 1;;
		-rm|--rm) 
			docker rm shopweb order user stock item;
			shift 1;;
		?|-?|-help|--help) show_usage; exit 0;;
		--) break;;
		*) echo " Invalid parameter: $1"; show_usage; exit 1;;
	esac
done
