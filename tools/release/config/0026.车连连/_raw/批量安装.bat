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
