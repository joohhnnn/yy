copy /B /Y src\MusicKuwoImpl.java ..\..\src\com\txznet\txz\component\music\kuwo\MusicKuwoImpl.java
copy /B /Y src\MusicManager.java ..\..\src\com\txznet\txz\module\music\MusicManager.java
copy /B /Y res\res_string_zh-CN.json ..\..\assets\data\res_string_zh-CN.json

del /S /Q ..\..\txz_libs\kwmusic-autosdk-v1.2.jar
del /S /Q ..\..\txz_libs\kwmusic-autosdk-v1.8.9.jar
copy /B /Y libs\kwmusic-autosdk-v1.8.9.jar ..\..\txz_libs\kwmusic-autosdk-v1.8.9.jar

pause