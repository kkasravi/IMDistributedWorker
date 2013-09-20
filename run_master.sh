#!/bin/bash
# Argument = -f frontendPort -b backendPort

usage()
{
cat << EOF
usage: $0 options

This script runs the akka IM server.

OPTIONS:
	-h	Show this message 
	-f	IM frontend port, optional. Default is 8080
	-b	IM backend port, optional. Default is 2552
EOF
}

BPORT=2552
FPORT=8080
while getopts “f:b:h” OPTION
do
     case $OPTION in
         h)
             usage
             exit 1
             ;;
         f)
             FPORT=$OPTARG
             ;;
         b)
             BPORT=$OPTARG
             ;;
         ?)
             usage
             exit
             ;;
     esac
done

IMHOST=`hostname -f`

java $PROTOTYPE_JAVA_OPTONS -cp target/scala-2.10/im-distributed-workers-assembly-0.1.jar -DimServer.imPort=$FPORT -DimServer.akka.remote.netty.tcp.port=$BPORT -DimServer.akka.remote.netty.tcp.hostname=$IMHOST com.intel.bigdata.prototype.frontend.server.IMServer