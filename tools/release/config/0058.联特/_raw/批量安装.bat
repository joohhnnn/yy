adb remount

adb uninstall com.txznet.txz
adb uninstall com.txznet.record
adb uninstall com.txznet.music
adb uninstall com.txznet.nav
adb uninstall com.txznet.webchat

adb uninstall com.txznet.settings
adb uninstall com.txznet.video

adb install -r ./TXZCore.apk
adb install -r ./TXZRecord.apk
adb install -r ./TXZMusic.apk
adb install -r ./TXZNav.apk
adb install -r ./TXZWebchat.apk

adb install -r ./TXZSettings.apk
adb install -r ./TXZVideo.apk


echo "��Ҫ��װLaucher����򿪸�Ӧ�ý�������������"
pause

adb uninstall com.txznet.launcher
adb install -r ./TXZLauncher.apk
echo "��װ��ɣ��س��������豸��"
pause

adb reboot
