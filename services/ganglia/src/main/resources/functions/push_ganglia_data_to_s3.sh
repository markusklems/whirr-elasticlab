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
function push_ganglia_data_to_s3() {
  MY_BUCKET=${1:-my-test-bucket-xyz1230987}
  MONITORING_DATA_DIR=${2:-/usr/local/monitoring-data}
  # make bucket
  s3cmd mb "s3://$MY_BUCKET"
  # put data in bucket
  s3cmd put --recursive "$MONITORING_DATA_DIR" "s3://$MY_BUCKET/"
}
