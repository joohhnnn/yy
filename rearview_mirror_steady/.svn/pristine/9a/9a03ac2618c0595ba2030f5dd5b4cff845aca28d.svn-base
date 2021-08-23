rmdir /s/q ..\..\assets\solibs\armeabi
rmdir /s/q ..\..\assets\solibs\armeabi-v7a
rmdir /s/q ..\..\libs\armeabi-v7a

set asset_so_dir=..\..\assets\solibs\x86
if exist %asset_so_dir% goto s1 
md %asset_so_dir%
:s1


set libs_so_dir=..\..\libs\x86
if exist %libs_so_dir% goto s2
md %libs_so_dir%
:s2

copy /B /Y txzlibs\*so   %asset_so_dir%\
copy /B /Y libs\*so   %libs_so_dir%\

del /S /Q ..\..\txz_libs\usc.jar
copy /B /Y yzslibs\usc.jar   ..\..\libs\usc.jar

del /S /Q ..\..\txz_libs\BaiduLBS_Android.jar
copy /B /Y libs\BaiduLBS_Android.jar   ..\..\txz_libs\BaiduLBS_Android.jar

del /S /Q ..\..\txz_libs\com.baidu.tts_2.3.0.jar
copy /B /Y libs\com.baidu.tts_2.3.0.jar   ..\..\txz_libs\com.baidu.tts_2.3.0.jar

copy /B /Y yzsres\backend_female   ..\..\assets\data\backend_female
copy /B /Y yzsres\frontend_model   ..\..\assets\data\frontend_model
copy /B /Y yzsres\txz.dat   ..\..\assets\data\txz.dat
copy /B /Y yzsres\bd_etts_speech_female.dat   ..\..\assets\data\bd_etts_speech_female.dat
copy /B /Y yzsres\bd_etts_speech_female_en.dat   ..\..\assets\data\bd_etts_speech_female_en.dat
copy /B /Y yzsres\bd_etts_text.dat   ..\..\assets\data\bd_etts_text.dat
copy /B /Y yzsres\bd_etts_text_en.dat  ..\..\assets\data\bd_etts_text_en.dat
copy /B /Y yzsres\bd_license_txz.dat ..\..\assets\data\bd_license_txz.dat

copy /B /Y src\WakeupYunzhishengImpl.java   ..\..\src\com\txznet\txz\component\wakeup\mix\WakeupYunzhishengImpl.java
copy /B /Y src\YZSAsrImpl.java   ..\..\src\com\txznet\txz\component\asr\mix\YZSAsrImpl.java
copy /B /Y src\YzsEngine.java   ..\..\src\com\txznet\txz\component\asr\txzasr\YzsEngine.java
copy /B /Y src\LocalAsrYunzhishengImpl.java   ..\..\src\com\txznet\txz\component\asr\mix\local\LocalAsrYunzhishengImpl.java
copy /B /Y src\FmManager.java   ..\..\src\com\txznet\txz\module\fm\FmManager.java
copy /B /Y src\ExchangeHelper.java   ..\..\src\com\txznet\txz\util\ExchangeHelper.java
copy /B /Y src\ProjectCfg.java   ..\..\src\com\txznet\txz\cfg\ProjectCfg.java
:copy /B /Y src\ImplCfg.java   ..\..\src\com\txznet\txz\cfg\ImplCfg.java
copy /B /Y src\AsrManager.java   ..\..\src\com\txznet\txz\module\asr\AsrManager.java
copy /B /Y src\WakeupManager.java   ..\..\src\com\txznet\txz\module\wakeup\WakeupManager.java
copy /B /Y src\NetAsrYunzhishengImpl.java   ..\..\src\com\txznet\txz\component\asr\mix\net\NetAsrYunzhishengImpl.java
copy /B /Y src\WakeupSenceYunzhishengImpl.java   ..\..\src\com\txznet\txz\component\wakeup\sence\WakeupSenceYunzhishengImpl.java
copy /B /Y src\ThreshHoldAdapter.java  ..\..\src\com\txznet\txz\util\ThreshHoldAdapter.java

copy /B /Y src\LocationServiceOfBaidu.java  ..\..\src\com\txznet\txz\module\location\LocationServiceOfBaidu.java
copy /B /Y src\PoiSearchToolBaiduImpl.java  ..\..\src\com\txznet\txz\component\poi\baidu\PoiSearchToolBaiduImpl.java
copy /B /Y src\PoiSearchBadiduImpl.java  ..\..\src\com\txznet\txz\component\map\baidu\PoiSearchBadiduImpl.java

copy /B /Y src\TtsBaiduFemaleImpl.java  ..\..\src\com\txznet\txz\component\tts\proxy\TtsBaiduFemaleImpl.java

copy /B /Y src\AsrContainer.java  ..\..\src\com\txznet\txz\component\asr\mix\AsrContainer.java

goto s2
if exist ..\..\src\com\unisound\common goto s1 
md ..\..\src\com\unisound\common
:s1
copy /B /Y src\aa.java   ..\..\src\com\unisound\common\aa.java

:s2

pause









