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
function setup_github() {
  
  # pk for github

cat >/home/users/markus/.ssh/git_id <<END_OF_FILE
-----BEGIN RSA PRIVATE KEY-----
MIIEowIBAAKCAQEA1zoYF//4fyE7+m44scoUn4uryESihPOXNJEo3NT7zzo8JKaj
p+weRiffibTkltv+Kxxq3QLHIofk9Ixmh1oJBNbPX8oaElVZADt1Wab5DKH51ECz
d/x0zE5WFg2fBer2BT3SY8acLIsREZk8vrrL6Ut5Ikoj1uBYHMIUr1jrV8uMnYuD
HDvAmfnMZX7Nt6FVXZaz7h3M5E9K+0zy8wctv0Ya0tE2wSkHG9PVgm08EFC0Aj7Q
2ET7OX/SmCoZULMA5sTsjEb/qCNDqNzxS9F81Nx2gIIjGAOUylKjNp6CLvrEtzzt
G18F06jeqWID4wfwfewUipNAxnytfr0Jcjo+rwIDAQABAoIBABnPMqJ2JKOCJcIj
Q5M2a4CybBgo+uA8oc5A9gh9VfEEx7+RaCCz9PFuyKSn152rpfAfiUUVlzGx4Cff
SlBC4L/+zoFrJ/M43uJjPvBQUJ9OMTpdw+fIk20Im9QrL/2yrmv0fO1QHNxOlO5q
VNl+ZwOD9HPDNkcHQim4sGhSnm45lyIVwg7bbhi9otncfvYBmBkAnjQ2jWHwz2/7
r2vBeieDJ8BODSRwdSfCyzWvssKgGPI6PIC3tgh3F7JxhEqQf3chJWCk8cm1XCZZ
hWuKya59vOMaHvkcL3WDGLllXe3aSwmt1XXeR2sccpWn0qt76n1nbuZY+mA8/kTC
vZOaIKECgYEA/Gu1bphxVCtyg9OPpTKtuVy9iIJiXRefVpsbP60c5SuDWJ0q8Ka2
yteax5ONxkYOx0m4kDWYjPfjYxOoX861vOdV01yFAJFg0C3vHqX7tSbZBAvVE4l1
xY8YHKRZ12J/ZSwAu6dojC46vStMsdzym9oB1T/WrB4QLn926HlPxj8CgYEA2kdf
DLWlUrmnL9SBIPIfOQYRxtZjRZFEDooc7/oWADr8eKy/Ngc9k4aDSrNlQpKw0imM
+LaaGX4hM9MJNYPJqtBYLqjj9QjpJ10qvnMkmiHNAOAB6fVzTuYxK1CxjUeKKlns
Dmm13xG+VpXAW0PKzUjOxwC7cxl5/3Hm51Zpy5ECgYA/ZppangWRG5yWyfPBjZoe
/dFJUV59fWpzKWBNvSCN20ERZ+CaM3WkO0VQLZqlm4EowzaNWjjn53eAsjZPYi85
tG1VOl7Zxqonf/IYo542YPyPTJ2HW7ZuGcP3pbw/IilXcxb4C1NyKkZsr0yDPDjf
toFRW8iC0MZFt5RZTecgpwKBgFn06f4x6iVSr80tdd4fCrEUiET628Sy3mpjjxNz
bJfBt4UAJHleuIjQ+dDSaBvsmoSybQ+fKZjx+zsBcR0l5/nGjWEFZ5T3jCcV5WTB
zUbcQUFoEEHcTx4oDgF86v9/iTh1AKzb8gExrdC/PJwgLzc1F0q6SYZg7H9S8MdM
PKORAoGBAMLOSeQL4nsUMmvsHCVx4hhWXw9gBLbethNlTMz3BUkSJujHvSAOO7IQ
WvHq9tWXlZEMZArnHckXV0yYBVy7ofGDDap62NWimcyhB2KFWjQ+EJvZ9366GMMY
FxK2zD/LBx0NHVdV55BwVLGXBptaLUwKXVSgIKruCOa9z0zHTtNZ
-----END RSA PRIVATE KEY-----
END_OF_FILE

cat >/root/.ssh/git_id <<END_OF_FILE
-----BEGIN RSA PRIVATE KEY-----
MIIEowIBAAKCAQEA1zoYF//4fyE7+m44scoUn4uryESihPOXNJEo3NT7zzo8JKaj
p+weRiffibTkltv+Kxxq3QLHIofk9Ixmh1oJBNbPX8oaElVZADt1Wab5DKH51ECz
d/x0zE5WFg2fBer2BT3SY8acLIsREZk8vrrL6Ut5Ikoj1uBYHMIUr1jrV8uMnYuD
HDvAmfnMZX7Nt6FVXZaz7h3M5E9K+0zy8wctv0Ya0tE2wSkHG9PVgm08EFC0Aj7Q
2ET7OX/SmCoZULMA5sTsjEb/qCNDqNzxS9F81Nx2gIIjGAOUylKjNp6CLvrEtzzt
G18F06jeqWID4wfwfewUipNAxnytfr0Jcjo+rwIDAQABAoIBABnPMqJ2JKOCJcIj
Q5M2a4CybBgo+uA8oc5A9gh9VfEEx7+RaCCz9PFuyKSn152rpfAfiUUVlzGx4Cff
SlBC4L/+zoFrJ/M43uJjPvBQUJ9OMTpdw+fIk20Im9QrL/2yrmv0fO1QHNxOlO5q
VNl+ZwOD9HPDNkcHQim4sGhSnm45lyIVwg7bbhi9otncfvYBmBkAnjQ2jWHwz2/7
r2vBeieDJ8BODSRwdSfCyzWvssKgGPI6PIC3tgh3F7JxhEqQf3chJWCk8cm1XCZZ
hWuKya59vOMaHvkcL3WDGLllXe3aSwmt1XXeR2sccpWn0qt76n1nbuZY+mA8/kTC
vZOaIKECgYEA/Gu1bphxVCtyg9OPpTKtuVy9iIJiXRefVpsbP60c5SuDWJ0q8Ka2
yteax5ONxkYOx0m4kDWYjPfjYxOoX861vOdV01yFAJFg0C3vHqX7tSbZBAvVE4l1
xY8YHKRZ12J/ZSwAu6dojC46vStMsdzym9oB1T/WrB4QLn926HlPxj8CgYEA2kdf
DLWlUrmnL9SBIPIfOQYRxtZjRZFEDooc7/oWADr8eKy/Ngc9k4aDSrNlQpKw0imM
+LaaGX4hM9MJNYPJqtBYLqjj9QjpJ10qvnMkmiHNAOAB6fVzTuYxK1CxjUeKKlns
Dmm13xG+VpXAW0PKzUjOxwC7cxl5/3Hm51Zpy5ECgYA/ZppangWRG5yWyfPBjZoe
/dFJUV59fWpzKWBNvSCN20ERZ+CaM3WkO0VQLZqlm4EowzaNWjjn53eAsjZPYi85
tG1VOl7Zxqonf/IYo542YPyPTJ2HW7ZuGcP3pbw/IilXcxb4C1NyKkZsr0yDPDjf
toFRW8iC0MZFt5RZTecgpwKBgFn06f4x6iVSr80tdd4fCrEUiET628Sy3mpjjxNz
bJfBt4UAJHleuIjQ+dDSaBvsmoSybQ+fKZjx+zsBcR0l5/nGjWEFZ5T3jCcV5WTB
zUbcQUFoEEHcTx4oDgF86v9/iTh1AKzb8gExrdC/PJwgLzc1F0q6SYZg7H9S8MdM
PKORAoGBAMLOSeQL4nsUMmvsHCVx4hhWXw9gBLbethNlTMz3BUkSJujHvSAOO7IQ
WvHq9tWXlZEMZArnHckXV0yYBVy7ofGDDap62NWimcyhB2KFWjQ+EJvZ9366GMMY
FxK2zD/LBx0NHVdV55BwVLGXBptaLUwKXVSgIKruCOa9z0zHTtNZ
-----END RSA PRIVATE KEY-----
END_OF_FILE

  chmod 600 /home/users/markus/.ssh/git_id
  chmod 600 /root/.ssh/git_id
  chown markus /home/users/markus/.ssh/git_id
  chown root /root/.ssh/git_id
  
  # github ssh settings
  echo -e "Host github.com\n\tStrictHostKeyChecking no\n\tIdentityFile /home/users/markus/.ssh/git_id\n" >> /home/users/markus/.ssh/config
  echo -e "Host github.com\n\tStrictHostKeyChecking no\n\tIdentityFile /home/users/markus/.ssh/git_id\n" >> /root/.ssh/config
  

}