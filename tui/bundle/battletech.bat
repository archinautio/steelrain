@echo off
rem Runs BattleTech with the Java runtime bundled next to this script.
"%~dp0runtime\bin\java.exe" -jar "%~dp0battletech.jar" %*
