#!/bin/bash
usage()
{
echo Usage: $0 num 
echo num: number of agents
exit 1
}

if [ $# -eq 0 ]; then
    usage 1
fi
java -cp target/scala-2.10/im-distributed-workers-assembly-0.1.jar com.intel.bigdata.prototype.frontend.server.IMServer 2>&1 > /dev/null&
master_pid=$!
sleep 3
num=$1
agent_pids=''
for i in $(seq $num);do
  java -cp  target/scala-2.10/im-distributed-workers-assembly-0.1.jar com.intel.bigdata.prototype.backend.worker.Main 2>&1 > /dev/null&
  agent_pids=$agent_pids' '$!
done
sleep 5
json=$(curl http://localhost:8090/start)
echo 'console.log(JSON.parse('$json').service.id)' > tmp.js
sleep 2
json=$(curl 'http://localhost:8090/info?service='$(node tmp.js))
echo $json
for i in $(echo $master_pid' '$agent_pids);do
kill $i
done
rm -f tmp.js
