adb remount

adb uninstall com.txznet.txz
adb uninstall com.txznet.record
adb uninstall com.txznet.music
adb uninstall com.txznet.nav
adb uninstall com.txznet.webchat

adb install ./TXZCore.apk
adb install ./TXZRecord.apk
adb install ./TXZMusic.apk
adb install ./TXZNav.apk
adb install ./TXZWebchat.apk

echo "将要安装Laucher，请打开个应用界面后按任意键继续"
pause

adb uninstall com.txznet.launcher
adb install ./TXZLauncher.apk
echo "安装完成，回车键重启设备！"
pause

adb reboot
