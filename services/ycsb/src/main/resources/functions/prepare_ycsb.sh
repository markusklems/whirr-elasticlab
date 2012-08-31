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
function prepare_ycsb() {
  # this is the experiment base directory
  EXPERIMENT_DIR=${1:-/usr/local/experiments}
  # the relative workload file path
  WORKLOAD_FILE=${2:-workloada}
  # this is the workload file path
  YCSB_WORKLOAD_FILE_PATH="$EXPERIMENT_DIR/$WORKLOAD_FILE"  
  # and this is where we store the benchmarking results locally
  BENCHMARK_BASE_DIR=${3:-/usr/local/benchmarking-data}
  BENCHMARK_DIR=`dirname "$BENCHMARK_BASE_DIR/$WORKLOAD_FILE"`

  # create directory for benchmarking data
  mkdir -p "$BENCHMARK_DIR" && echo "created $BENCHMARK_DIR"

  # set the environment variables
  cp /etc/profile-copy /etc/profile
  echo "export ENV_YCSB_WORKLOAD_FILE_PATH=$YCSB_WORKLOAD_FILE_PATH" >> /etc/profile
  echo "export env variable: ENV_YCSB_WORKLOAD_FILE_PATH=$YCSB_WORKLOAD_FILE_PATH"
  #echo "export ENV_BENCHMARK_DIR=$BENCHMARK_DIR" >> /etc/profile
  #echo "export env variable: ENV_BENCHMARK_DIR=$BENCHMARK_DIR"
  #echo "export ENV_EXPERIMENT_DIR=$EXPERIMENT_DIR" >> /etc/profile
  #echo "export env variable: ENV_EXPERIMENT_DIR=$EXPERIMENT_DIR"
  
  # now source the file to refresh the shell with the new env variables
  source /etc/profile
}