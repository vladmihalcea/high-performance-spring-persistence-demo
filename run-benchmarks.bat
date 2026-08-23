@echo off
set JAVA_HOME=%JAVA_HOME_25%

call mvn -P benchmark-tests clean integration-test

