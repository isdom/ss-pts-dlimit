#!/bin/bash

export NACOS_ENDPOINT=
export NACOS_NAMESPACE=
export NACOS_DATAID=pts-dlimit

export SLS_ENDPOINT=cn-beijing-intranet.log.aliyuncs.com
export SLS_PROJECT=
export SLS_LOGSTORE=
export SLS_TOPIC=pts-dlimit-dev

export CPB_GROUP=DEFAULT_GROUP
export CPB_DATAID=sls-access.conf

export JVM_MEM=1024M
export JVM_DIRECT_MEM=128M
export JVM_PID_FILE=pts-dlimit.pid
export JVM_BOOT_JAR=pts-dlimit-1.0-SNAPSHOT.jar
export JVM_SPRING_PROFILE=dev