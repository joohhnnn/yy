adb remount

adb shell rm -rf /system/app/TXZ*
adb shell rm -rf /system/app/txz*

adb uninstall com.txznet.txz
adb uninstall com.txznet.record
adb uninstall com.txznet.music
adb uninstall com.txznet.nav
adb uninstall com.txznet.webchat

adb uninstall com.txznet.settings

adb install ./TXZCore.apk
adb install ./TXZRecord.apk
adb install ./TXZMusic.apk
adb install ./TXZNav.apk
adb install ./TXZWebchat.apk

adb install ./TXZSettings.apk


echo "将要安装Laucher，请打开个应用界面后按任意键继续"
pause

adb uninstall com.txznet.launcher
adb install ./TXZLauncher.apk
echo "安装完成，回车键重启设备！"
pause

adb reboot
