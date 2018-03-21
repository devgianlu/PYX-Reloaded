@echo off
pushd %~dp0

ren PYX-Reloaded.jar PYX-Reloaded.old.jar
java -jar ./PYX-Reloaded.old.jar --update
del PYX-Reloaded.old.jar
pause

popd