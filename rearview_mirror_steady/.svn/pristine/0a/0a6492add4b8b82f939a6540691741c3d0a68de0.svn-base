::copy /B /Y libs\armeabi-v7a\libtxzComm.so        libtxzComm.so.bak
::copy /B /Y libs\armeabi-v7a\libtxzComm.so.debug  libtxzComm.so.debug.bak

del /S /Q src
del /S /Q txz_libs
del /S /Q libs
del /S /Q assets
del /S /Q res

xcopy /S /Y ..\TXZCore\src\* .\src\*
xcopy /S /Y ..\TXZCore\txz_libs\* .\txz_libs\*
xcopy /S /Y ..\TXZCore\assets\* .\assets\*
xcopy /S /Y ..\TXZCore\res\* .\res\*

xcopy /S /Y x86libs\armeabi-v7a\* libs\armeabi-v7a\*
copy /B /Y x86libs\libtxzComm.so   assets\solibs\armeabi-v7a\libtxzComm.so
copy /B /Y x86libs\libtxzComm.so.debug  libs\txzComm.debug\libtxzComm.so.debug

::del /S /Q libtxzComm.so.bak
::del /S /Q libtxzComm.so.debug.bak



