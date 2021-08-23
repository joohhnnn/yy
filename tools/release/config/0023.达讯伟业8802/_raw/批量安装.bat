adb remount

adb push ./phone_area.data /system/lib/
adb push ./pinyin.data /system/lib
adb push ./libtxzutil.so /system/lib

adb uninstall com.txznet.txz
adb uninstall com.txznet.record
adb uninstall com.txznet.bluetooth
adb uninstall com.txznet.webchat

adb install -r ./TXZCore.apk
adb install -r ./TXZRecord.apk
adb install -r ./TXZWebchat.apk
adb install -r ./TXZBluetooth.apk

echo "安装完成，回车键重启设备！"
pause

adb reboot
