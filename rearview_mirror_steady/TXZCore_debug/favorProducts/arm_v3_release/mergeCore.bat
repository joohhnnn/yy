copy /B /Y yzslibs\libaec.so   ..\..\assets\solibs\armeabi-v7a\libaec.so
copy /B /Y yzslibs\libasrfix.so   ..\..\assets\solibs\armeabi-v7a\libasrfix.so
copy /B /Y yzslibs\libuscasr.so   ..\..\assets\solibs\armeabi-v7a\libuscasr.so
copy /B /Y yzslibs\libyzstts.so   ..\..\assets\solibs\armeabi-v7a\libyzstts.so
copy /B /Y yzslibs\usc.jar   ..\..\txz_libs\usc.jar

copy /B /Y yzsres\backend_female   ..\..\assets\data\backend_female
copy /B /Y yzsres\frontend_model   ..\..\assets\data\frontend_model
copy /B /Y yzsres\txz.dat   ..\..\assets\data\txz.dat
copy /B /Y src\WakeupYunzhishengImpl.java   ..\..\src\com\txznet\txz\component\wakeup\mix\WakeupYunzhishengImpl.java
copy /B /Y src\YZSAsrImpl.java   ..\..\src\com\txznet\txz\component\asr\mix\YZSAsrImpl.java
copy /B /Y src\TxzEngine.java   ..\..\src\com\txznet\txz\component\asr\txzasr\TxzEngine.java
copy /B /Y src\YzsEngine.java   ..\..\src\com\txznet\txz\component\asr\txzasr\YzsEngine.java
copy /B /Y src\LocalAsrYunzhishengImpl.java   ..\..\src\com\txznet\txz\component\asr\mix\local\LocalAsrYunzhishengImpl.java
copy /B /Y src\FmManager.java   ..\..\src\com\txznet\txz\module\fm\FmManager.java
copy /B /Y src\ExchangeHelper.java   ..\..\src\com\txznet\txz\util\ExchangeHelper.java

if exist ..\..\src\com\unisound\common goto s1 
md ..\..\src\com\unisound\common
:s1
copy /B /Y src\v.java   ..\..\src\com\unisound\common\v.java

pause









