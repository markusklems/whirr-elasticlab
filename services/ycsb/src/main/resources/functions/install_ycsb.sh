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
  Y_DB=${3:-basic}
  Y_WORKLOADS_DIR=$YCSB_HOME/workloads
  Y_WORKLOAD_FILE=$Y_WORKLOADS_DIR/${4:-workloada}
  Y_REPORT_FILE=$YCSB_HOME/reports/${4:-workloada} 
 
  y_tar_file=`basename $Y_TAR_URL`
  y_tar_dir=`echo $y_tar_file | awk -F '-bin' '{print $1}'`
  
  YCSB_HOME=/usr/local/$y_tar_dir
  
  install_tarball $Y_TAR_URL
  
  echo "export YCSB_HOME=$YCSB_HOME" >> /etc/profile
  echo 'export PATH=$YCSB_HOME/bin:$PATH' >> /etc/profile
  
  cat >/etc/init.d/ycsb <<END_OF_FILE
#!/bin/bash

LOAD="$YCSB_HOME/bin/ycsb load $Y_DB -P $Y_WORKLOAD_FILE"
RUN="$YCSB_HOME/bin/ycsb run $Y_DB -P $Y_WORKLOAD_FILE -s > $Y_REPORT_FILE"

PIDFILE=/var/run/ycsb.pid

running(){
    PID=\`cat \$PIDFILE 2>/dev/null\`

    # Check that the pid is sane.
    if [ "x\$PID" == "x" ] ; then
        false
    else
        # Check that the process is alive.
        ps \$PID >/dev/null 2>&1
    fi
}

load(){
    echo -n $"Starting YCSB LOAD phase: "

    # Try to start the load phase.
    if running; then
        echo "Failed. Maybe remove \$PIDFILE?"
        false
    else
        mkdir -p \`dirname \$Y_WORKLOAD_FILE\`
        \$LOAD
        PID=\$!
        mkdir -p \`dirname \$PIDFILE\`
        echo \$PID > \$PIDFILE

        echo "Success."
        # Clear out the pidfile.
        echo "Success."
        rm -f \$PIDFILE
        true
    fi
}

run(){
    echo -n $"Starting YCSB RUN phase: "
    # Try to start the run phase.
    if running; then
        echo "Failed. Maybe remove \$PIDFILE?"
        false
    else
        mkdir -p \`dirname \$Y_WORKLOAD_FILE\`
        mkdir -p \`dirname \$Y_REPORT_FILE\`        
        \$RUN
        PID=\$!
        mkdir -p \`dirname \$PIDFILE\`
        echo \$PID > \$PIDFILE

        echo "Success."
        # Clear out the pidfile.
        echo "Success."
        rm -f \$PIDFILE
        true
    fi
}

status(){
    echo -n $"Status of YCSB: "

    if running ; then
        echo "Running."
        true
    else
        echo "Not running."
        false
    fi
}

# See how we were called.
case "\$1" in
    load)
 load
 RETVAL=\$?
 ;;
    run)
 run
 RETVAL=\$?
 ;;
    *)
 echo $"Usage: \$0 {load|run|status}"
 RETVAL=2
esac

exit
END_OF_FILE

  chmod +x /etc/init.d/ycsb
  install_service ycsb

}

