::python tools\pre_java.py
::rmdir _gen_src
::mkdir _gen_src
::xcopy /S /Y src\* _gen_src\*
::xcopy /S /Y pbsrc\* _gen_src\*
::xcopy /S /Y src_sdk\* _gen_src\*
call ant -buildfile build_uilib.xml clean
call ant -buildfile build_uilib.xml release
::copy bin\classes.jar bin\txzcomm.jar
::call ..\..\tools\proguard5.2.1\bin\proguard.bat @proguard_uilib.pro
pause


