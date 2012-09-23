@echo off
rem -------------------------------------------------------------------------
rem Start Script for JMX MBean Poller
rem -------------------------------------------------------------------------

set CONFIG_FILE_NAME=%1

if "x%JAVA_HOME%" == "x" (
  set  JAVA=java
  echo JAVA_HOME is not set. Unexpected results may occur.
  echo Set JAVA_HOME to the directory of your local JRE to avoid this message.
) else (
  set "JAVA=%JAVA_HOME%\bin\java"
  
)


set SPLUNK4JMX_HOME=%SPLUNK_HOME%/etc/apps/SPLUNK4JMX
set MAIN_CLASS=com.dtdsoftware.splunk.JMXMBeanPoller
set LIB_DIR=%SPLUNK4JMX_HOME%/bin/lib
set POLLER_JARS=%LIB_DIR%/*;%LIB_DIR%/ext/*
set JVM_MEMORY="-Xms64m -Xmx64m"
set JAVA_OPTS="%JVM_MEMORY%"
set CONFIG_XML=%SPLUNK4JMX_HOME%/bin/config/%CONFIG_FILE_NAME%


"%JAVA%"  "-Dsplunk4jmx.home=%SPLUNK4JMX_HOME%"  %JAVA_OPTS% -classpath "%LIB_DIR%;%POLLER_JARS%"  %MAIN_CLASS% "%CONFIG_XML%"
