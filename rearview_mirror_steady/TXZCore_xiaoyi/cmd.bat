@echo off
set "PATH=%PATH%;jni\tools;"
set "DEBUGOBJ=obj/local/armeabi/libtxzComm.so"
call vcvarsall.bat
echo **************************************************
echo DEBUGOBJ = %DEBUGOBJ%
echo **************************************************
cmd.exe

