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
adb uninstall com.tencent.qqmusic

adb install ./TXZCore.apk
adb install ./TXZRecord.apk
adb install ./TXZWebchat2.apk
adb install ./TXZBluetooth.apk
adb install ./TXZMusic.apk
adb install ./TXZNav.apk
adb install ./TXZLauncher.apk

echo "安装完成，回车键重启设备！"
pause

adb reboot
