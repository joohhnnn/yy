adb remount

adb shell rm -rf /system/app/TXZ*
adb shell rm -rf /system/app/txz*
adb shell rm -rf /system/app/Camera*

adb push ./phone_area.data /system/lib/
adb push ./pinyin.data /system/lib
adb push ./libtxzutil.so /system/lib
adb push ./TXZAdapter.apk /system/app

adb uninstall com.txznet.txz
adb uninstall com.txznet.record
adb uninstall com.txznet.music
adb uninstall com.txznet.nav
adb uninstall com.txznet.webchat

adb uninstall com.txznet.settings
adb uninstall com.txznet.video

adb uninstall com.txznet.bluetooth
adb uninstall com.txznet.fm

adb uninstall com.android.camera2

adb install -r ./TXZCore.apk
adb install -r ./TXZRecord.apk
adb install -r ./TXZMusic.apk
adb install -r ./TXZNav.apk
adb install -r ./TXZWebchat.apk

adb install -r ./TXZSettings.apk
adb install -r ./TXZVideo.apk

adb install -r ./TXZBluetooth_S5.apk
adb install -r ./TXZFM.apk

adb install -r ./Camera2.apk


echo "将要安装Laucher，请打开个应用界面后按任意键继续"
pause

adb uninstall com.txznet.launcher
adb install -r ./TXZLauncher.apk
echo "安装完成，回车键重启设备！"
pause

adb reboot
