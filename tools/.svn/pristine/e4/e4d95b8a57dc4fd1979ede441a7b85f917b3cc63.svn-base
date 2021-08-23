adb remount

adb push ./phone_area.data /system/lib/
adb push ./pinyin.data /system/lib
adb push ./libtxzutil.so /system/lib

adb uninstall com.txznet.txz
adb uninstall com.txznet.record
adb uninstall com.txznet.bluetooth
adb uninstall com.txznet.webchat
adb uninstall com.txznet.nav
adb uninstall com.txznet.music


adb push ./TXZCore.apk /system/app
adb push ./TXZRecord.apk /system/app
adb push ./TXZWebchat.apk /system/app
adb push ./TXZBluetooth.apk /system/app

copy TXZCore.apk TXZCorerar.apk
ren TXZCorerar.apk TXZCore.rar
"D:\Program Files\WinRAR\Rar.exe" x -t -o-p ./TXZCore.rar ./TXZCore/
adb push ./TXZCore/lib/armeabi /system/lib
adb push ./TXZCore/lib/armeabi-v7a /system/lib


echo "安装完成，回车键重启设备！"
pause

adb reboot
