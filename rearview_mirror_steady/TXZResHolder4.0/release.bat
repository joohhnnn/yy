call ant clean
call ant release
adb push /Users/xiaolin/txz/svn/android/projects/rearview_mirror_steady/TXZResHolder8.0/build/outputs/apk/debug/TXZResHolder8.0-debug.apk /sdcard/txz/resource/ResHolder.apk
adb shell am force-stop com.txznet.txz
adb shell am start -n com.txznet.txz/.module.ui.TestActivity
pause


