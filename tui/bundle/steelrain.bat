@echo off
rem Runs Steelrain with the Java runtime bundled next to this script.
"%~dp0runtime\bin\java.exe" -jar "%~dp0steelrain.jar" %*
