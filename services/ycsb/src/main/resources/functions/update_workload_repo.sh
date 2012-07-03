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
function update_workload_repo() {
  YCSB_WORKLOAD_REPO=${1:-git://github.com/markusklems/YCSB-workloads.git}
  #YCSB_WORKLOAD_REPO_VERSION=${2:-HEAD}
  
  # remove the existing workload files
  rm -Rd /usr/local/ycsb-0.1.4/workloads
  
  # clone repo from git
  git clone $YCSB_WORKLOAD_REPO /usr/local/ycsb-0.1.4/workloads
  
}