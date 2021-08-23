adb remount

adb shell rm -rf /system/app/Txz*
adb shell rm -rf /system/app/TXZ*

adb push ./Launcher_PX2.apk /system/app/
adb push ./FireEye.apk /system/app/
adb push ./TxzInitService_all.apk /system/app/

adb uninstall com.txznet.txz
adb uninstall com.txznet.music
adb uninstall com.txznet.nav
adb uninstall com.txznet.webchat

adb install ./TXZCore.apk
adb install ./TXZMusic.apk
adb install ./TXZNav.apk
adb install ./TXZWebchat.apk

echo "安装完成，回车键重启设备！"
pause

adb reboot
