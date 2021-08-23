set project=xiaofang
set arch=armeabi-v7a

del -q .\libs\armeabi-v7a\libasrfix.so 
del -q .\libs\armeabi-v7a\libuscasr.so
del -q .\libs\usc.jar

copy .\extends\libs\%project%\%arch%\libasrfix.so  .\libs\armeabi-v7a\
copy .\extends\libs\%project%\%arch%\libuscasr.so  .\libs\armeabi-v7a\
copy .\extends\libs\%project%\usc.jar  .\libs\usc.jar

call ant clean
call ant release
md .\releaseApp\%project%\%arch%\
del -q .\releaseApp\%project%\%arch%\*.apk
copy .\bin\*.apk .\releaseApp\%project%\%arch%\



