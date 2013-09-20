#!/bin/bash
usage()
{
echo Usage: $0 command num 
echo 'command: start|status|config'
echo 'num: number of agents'
exit 1
}

if [ $# -eq 0 -o $# -eq 1 ]; then
  usage 1
fi
command=$1
if [ $command != 'start' -a $command != 'config' -a $command != 'status' ]; then
  usage 1
fi
num=$2
rm -f server.out worker.*.out
java -cp target/scala-2.10/im-distributed-workers-assembly-0.1.jar com.intel.bigdata.prototype.frontend.server.IMServer 2>&1 > server.out&
master_pid=$!
sleep 3
agent_pids=''
for i in $(seq $num);do
  java -cp  target/scala-2.10/im-distributed-workers-assembly-0.1.jar com.intel.bigdata.prototype.backend.worker.Main 2>&1 > worker.$i.out&
  agent_pids=$agent_pids' '$!
done
registeredworkers=0
while [ $registeredworkers -lt $num ]; do
registeredworkers=$(cat server.out|grep 'Worker registered'|wc -l)
sleep 3
done
echo registeredworkers =$registeredworkers running service...
sleep 3
json=$(curl http://localhost:8090/$command)
echo 'console.log(JSON.parse('$json').service.id)' > service.js
service=$(node service.js)
echo $service
sleep 2
workers=0
while [ $workers -lt $num ]; do
echo workers=$workers num=$num
json=$(curl 'http://localhost:8090/info?service='$service)
echo "console.log(JSON.parse('"$json"').service.timesPerWorker.length)" > workers.js
workers=$(node workers.js)
#cat workers.js
sleep 1
done
echo $json
for i in $(echo $master_pid' '$agent_pids);do
kill $i
done
#rm -f service.js workers.js
