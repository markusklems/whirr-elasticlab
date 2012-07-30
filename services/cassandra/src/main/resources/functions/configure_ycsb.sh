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
function configure_ycsb() {
  echo 'export PATH=${1:-apache-cassandra-1.0}/bin:$PATH' >> /etc/profile
  source /etc/profile

  CREATE_TABLE_STATEMENTS=/usr/local/create_usertable

  cat >$CREATE_TABLE_STATEMENTS <<END_OF_FILE
create keyspace usertable with strategy_options = [{replication_factor:3}] and placement_strategy = 'org.apache.cassandra.locator.SimpleStrategy';
use usertable;
create column family data with comparator='AsciiType';
END_OF_FILE
  
  cassandra-cli -f $CREATE_TABLE_STATEMENTS
}
