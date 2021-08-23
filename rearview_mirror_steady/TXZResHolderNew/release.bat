call ant clean
call ant release
adb push bin\ResHolder-release.apk /sdcard/txz/resource/ResHolder.apk
adb shell am force-stop com.txznet.txz
adb shell am start -n com.txznet.txz/.module.ui.TestActivity
pause


