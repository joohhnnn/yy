@echo off

cls

setlocal EnableDelayedExpansion


echo ==========同行者系统应用安装脚本 by bihongpi==========
echo ===========================================================

SET TEMP_DIR=__pibihong_temp_%RANDOM%

SET APKS=%*

if "%APKS%"=="" (
    echo 用法：install_sys_apk apk文件列表
    echo 例如：install_sys_apk TXZCore.apk TXZMusic.apk
    goto END
)


:MUTIL_DEV

SET DEV=
SET /a c=0
FOR /f %%f in ('adb devices^|find /V "devices"^|find "device"') do (
    SET /a c+=1
    SET DEV=%%f
)

::echo ==========共找到%c%个设备

IF %c%==0 (
    adb devices
    echo ==========没有找到在线设备，请使用adb devices检查连接是否正常！！！
    goto END
)

IF NOT %c%==1 (
    SET /a c=0
    FOR /f %%f in ('adb devices^|find /V "devices"^|find "device"') do (
        SET /a c+=1
        ECHO !c!  %%f
    )
    SET /p o=存在!c!个设备，请问你要选择第几个：
    IF NOT DEFINED o (
        GOTO MUTIL_DEV
    )
    IF !o!=="" (
        GOTO MUTIL_DEV
    )
    SET DEV=
    SET /a c=0
    FOR /f %%f in ('adb devices^|find /V "devices"^|find "device"') do (
        SET /a c+=1
        IF !c!==!o! SET DEV=%%f
    )
    IF NOT DEFINED DEV (
        GOTO MUTIL_DEV
    )
    IF !DEV!=="" (
        GOTO MUTIL_DEV
    )
)

:BEGIN

ECHO 使用%DEV%进行安装

echo ===========================================================

ECHO ==========开始重新装载系统分区...
adb -s %DEV% remount

IF ERRORLEVEL 1 (
    echo ==========重新装载系统分区发生错误！！！
    goto END
)


::判断是否为android5.0
SET /a APK_COUNT=0
SET /a DIR_COUNT=0
FOR /f %%f in ('adb shell ls /system/app^|find ".apk"') do (
	set /a APK_COUNT+=1
)
FOR /f %%f in ('adb shell ls /system/app^|find /V ".apk"') do (
	set /a DIR_COUNT+=1
)
if %DIR_COUNT% EQU %APK_COUNT% (
    echo ==========无法确定系统应用安装方式！！！
    goto END
)
SET /a DIR_INSTALL=0
if %DIR_COUNT%  GTR %APK_COUNT% (
    SET /a DIR_INSTALL=1
)


IF EXIST %TEMP_DIR% RD /S /Q %TEMP_DIR%
ECHO ==========开始创建临时工作目录...
MD %TEMP_DIR%
IF ERRORLEVEL 1 (
    echo ==========创建临时工作目录发生错误！！！
    goto END
)

FOR %%f in (%APKS%) do (
    SET APK_FILE=%%~dpnxf
	IF EXIST %TEMP_DIR%/lib RD /S /Q %TEMP_DIR%/lib
    ECHO ==========开始解压[%%f]...
    start /min /D %TEMP_DIR% /wait jar -xvf "!APK_FILE!" lib
    IF ERRORLEVEL 1 (
        echo ==========解压[%%f]发生错误！！！
        goto END
    )
	adb -s %DEV% shell rm -rf /system/app/%%~nf
	adb -s %DEV% shell rm -rf /system/app/%%~nxf
	IF %DIR_INSTALL%==1 (
	    adb -s %DEV% shell mkdir -p /system/app/%%~nf/lib/arm
	)
	FOR /R %TEMP_DIR%\lib %%k in (*.so) DO (
		ECHO ==========开始上传动态库[%%~nxk]...
		if %DIR_INSTALL%==0 (
		    adb -s %DEV% push "%%k" /system/lib/%%~nxk
		) else (
		    adb -s %DEV% push "%%k" /system/app/%%~nf/lib/arm/%%~nxk
		)
		IF ERRORLEVEL 1 (
			echo ==========上传动态库[%%~nxk]发生错误！！！
			goto END
		)
	)
	ECHO ==========开始上传apk[%%f]...
	if %DIR_INSTALL%==0 (
		adb -s %DEV% push "%%f" /system/app/%%~nxf
	) else (
		adb -s %DEV% push "%%f" /system/app/%%~nf/%%~nxf
	)
    IF ERRORLEVEL 1 (
        echo ==========上传apk[%%f]发生错误！！！
        goto END
    )
)


ECHO ==========开始清理目录...
RD /S /Q %TEMP_DIR%


echo ===========================================================
set o=
set /p o=安装完成，需要立即重启吗? (Y/N):
IF NOT DEFINED o goto END
IF /i "%o%"=="Y" goto YES
goto END
:YES
echo 系统正在重启，请稍后...
adb reboot
IF ERRORLEVEL 1 (
    echo ==========重启发生错误，请自行手工重启设备！！！
    goto END
)


:END
IF EXIST %TEMP_DIR% RD /S /Q %TEMP_DIR%
pause

