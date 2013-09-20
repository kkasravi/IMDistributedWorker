#!/bin/bash
# Argument = -n IMHostname -p IMPort -a agentPort

usage()
{
cat << EOF
usage: $0 options

This script runs the akka distributed workers IM agent.

OPTIONS:
	-h	Show this message 
	-n	IM master host name (FQDN), mandatory.
	-p	IM master backend port, optional. Default is 2552
	-a	IM agent port, optional. Default is 10000
EOF
}

BPORT=2552
APORT=10000
IMHOST=
while getopts “n:p:a:h” OPTION
do
     case $OPTION in
         h)
             usage
             exit 1
             ;;
         a)
             APORT=$OPTARG
             ;;
         p)
             BPORT=$OPTARG
             ;;
         n)
             IMHOST=$OPTARG
             ;;
         ?)
             usage
             exit
             ;;
     esac
done

if [[ -z $IMHOST ]]
then
     usage
     exit 1
fi

AHOST=`hostname -f`

java $PROTOTYPE_JAVA_OPTONS -cp target/scala-2.10/im-distributed-workers-assembly-0.1.jar -DimServer.akka.remote.netty.tcp.hostname=$IMHOST -DimServer.akka.remote.netty.tcp.port=$BPORT -DimAgent.akka.remote.netty.tcp.hostname=$AHOST -DimAgent.akka.remote.netty.tcp.port=$APORT com.intel.bigdata.prototype.backend.worker.Main