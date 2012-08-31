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
function install_ycsb() {

  Y_MAJOR_VERSION=${1:-0.1.4}
  Y_TAR_URL=${2:-https://github.com/downloads/brianfrankcooper/YCSB/ycsb-0.1.4.tar.gz}
  YCSB_HOME=/usr/local/ycsb
 
  y_tar_file=`basename $Y_TAR_URL`
  y_tar_dir=`echo $y_tar_file | awk -F '.tar.gz' '{print $1}'`
  
  install_tarball_no_md5 $Y_TAR_URL
  mv /usr/local/ycsb-$Y_MAJOR_VERSION $YCSB_HOME
 
  echo "export YCSB_HOME=$YCSB_HOME" >> /etc/profile
  #echo 'export PATH=$ENV_YCSB_HOME/bin:$PATH' >> /etc/profile 
  source /etc/profile
  cp /etc/profile /etc/profile-copy

}