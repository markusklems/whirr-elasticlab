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
function append_hosts_to_workload_file() {
  YCSB_WORKLOAD_FILE_PATH="$ENV_YCSB_WORKLOAD_FILE_PATH"

  servers=""
  for server in "$@"; do
    servers="${servers}${server},"
  done
  #remove last comma
  servers=${servers%?}
  
  # add a blank line and then the hosts line
  # -F: file to search
  # -q: silent search

  # search if the line "hosts=..." already exists in file
  if grep -Fq "hosts=" "$YCSB_WORKLOAD_FILE_PATH"
  then
      echo "hosts line already exists in workload file $YCSB_WORKLOAD_FILE_PATH, replace the old line with a new line."
      sed -i 's/hosts=.*/hosts='$servers'/g' "$YCSB_WORKLOAD_FILE_PATH"  
  else
    echo "hosts line not found in workload file $YCSB_WORKLOAD_FILE_PATH. Write a new line with hosts=$servers."
    echo "hosts=$servers" >> "$YCSB_WORKLOAD_FILE_PATH"
  fi
}