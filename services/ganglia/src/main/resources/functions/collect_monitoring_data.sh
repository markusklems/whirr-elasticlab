#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
function collect_monitoring_data() {
  echo "start collecting monitoring data"
  #rm -Rd $1
  mkdir $1
  echo "created directory $1"
  start=`cat /usr/local/start_time`
  end=`cat /usr/local/end_time`
  echo "time between $start and $end"
  CLUSTER="/var/lib/ganglia/rrds/${2:-ycsb-cassandra-cluster}/*"
  for server in $CLUSTER
  do
      server_name=`echo "$server" | awk -F"/" '{print $NF}'`
      server_dir="${1:-/usr/local/ycsb-0.1.4/workloads/reports/monitoring/}$server_name/"
      mkdir $server_dir
      SERVER="$server/*"
      echo "processing directory $SERVER"
      for f in $SERVER
      do
          echo "Fetching file $f"
          fetched=`echo "$f" | awk -F"/" '{print $NF}' | awk -F".rrd" '{print $1}'`
          #echo "After processing: $fetched"
          rrdtool fetch "$f" AVERAGE sum -s "$start" -e "$end" >> \
          "$server_dir$fetched"
      done
  done
}