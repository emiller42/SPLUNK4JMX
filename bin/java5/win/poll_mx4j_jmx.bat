@echo off
rem -------------------------------------------------------------------------
rem Start Script for JMX MBean Poller using MX4J JMX implementation
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
set POLLER_JARS=%LIB_DIR%/axis.jar;%LIB_DIR%/saaj.jar;%LIB_DIR%/slf4j-api-1.6.4.jar;%LIB_DIR%/castor-1.3-core.jar;%LIB_DIR%/slf4j-log4j12-1.6.4.jar;%LIB_DIR%/castor-1.3-xml.jar;%LIB_DIR%/splunk.jar;%LIB_DIR%/commons-discovery-0.2.jar;%LIB_DIR%/splunklogging.jar;%LIB_DIR%/commons-logging-1.1.1.jar;%LIB_DIR%/tools-lin.jar;%LIB_DIR%/hessian-3.0.8.jar;%LIB_DIR%/tools-win.jar;%LIB_DIR%/jaxrpc.jar;%LIB_DIR%/wsdl4j.jar;%LIB_DIR%/log4j-1.2.16.jar;%LIB_DIR%/xercesImpl.jar;%LIB_DIR%/mx4j-tools.jar
set JVM_MEMORY="-Xms64m -Xmx64m"
set JAVA_OPTS="%JVM_MEMORY%"
set CONFIG_XML=%SPLUNK4JMX_HOME%/bin/config/%CONFIG_FILE_NAME%

set MX4J_JARS_FOR BOOTPATH=%LIB_DIR%/boot/mx4j.jar;%LIB_DIR%/boot/mx4j-remote.jar

set BOOTPATH=%MX4J_JARS_FOR BOOTPATH%

"%JAVA%" "-Xbootclasspath/a:%BOOTPATH%"  "-Dsplunk4jmx.home=%SPLUNK4JMX_HOME%"  %JAVA_OPTS% -classpath "%LIB_DIR%;%POLLER_JARS%"  %MAIN_CLASS% "%CONFIG_XML%"
