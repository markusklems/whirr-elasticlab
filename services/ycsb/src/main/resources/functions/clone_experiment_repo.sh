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
function clone_experiment_repo() {
  # the read-only link to the experiment repo on github
  REMOTE_EXPERIMENT_REPO=${1:-git://github.com/markusklems/Experiments.git}
  EXPERIMENT_DIR=${2:-/usr/local/experiments}
  
  # remove the existing experiment files (potentially from a previous experiment run)
  rm -Rd "$EXPERIMENT_DIR"
  
  # clone repo from git in read-only mode
  git clone "$REMOTE_EXPERIMENT_REPO" "$EXPERIMENT_DIR"
  
}
