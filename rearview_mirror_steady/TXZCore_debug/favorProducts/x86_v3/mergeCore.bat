xcopy /S /Y txzlibs\armeabi-v7a\* ..\..\libs\armeabi-v7a\*
copy /B /Y txzlibs\libtxzComm.so   ..\..\assets\solibs\armeabi-v7a\libtxzComm.so
copy /B /Y txzlibs\libtxzComm.so.debug  ..\..\libs\txzComm.debug\libtxzComm.so.debug

copy /B /Y yzslibs\libaec.so   ..\..\assets\solibs\armeabi-v7a\libaec.so
copy /B /Y yzslibs\libasrfix.so   ..\..\assets\solibs\armeabi-v7a\libasrfix.so
copy /B /Y yzslibs\libuscasr.so   ..\..\assets\solibs\armeabi-v7a\libuscasr.so
copy /B /Y yzslibs\libyzstts.so   ..\..\assets\solibs\armeabi-v7a\libyzstts.so
copy /B /Y txzlibs\libmsc.so   ..\..\assets\solibs\armeabi-v7a\libmsc.so

copy /B /Y yzslibs\usc.jar   ..\..\txz_libs\usc.jar

copy /B /Y yzsres\backend_female   ..\..\assets\data\backend_female
copy /B /Y yzsres\frontend_model   ..\..\assets\data\frontend_model
copy /B /Y yzsres\txz.dat   ..\..\assets\data\txz.dat
copy /B /Y src\WakeupYunzhishengImpl.java   ..\..\src\com\txznet\txz\component\wakeup\mix\WakeupYunzhishengImpl.java

pause









