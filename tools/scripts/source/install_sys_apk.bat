@echo off

cls

setlocal EnableDelayedExpansion


echo ==========ͬ����ϵͳӦ�ð�װ�ű� by bihongpi==========
echo ===========================================================

SET TEMP_DIR=__pibihong_temp_%RANDOM%

SET APKS=%*

if "%APKS%"=="" (
    echo �÷���install_sys_apk apk�ļ��б�
    echo ���磺install_sys_apk TXZCore.apk TXZMusic.apk
    goto END
)


:MUTIL_DEV

SET DEV=
SET /a c=0
FOR /f %%f in ('adb devices^|find /V "devices"^|find "device"') do (
    SET /a c+=1
    SET DEV=%%f
)

::echo ==========���ҵ�%c%���豸

IF %c%==0 (
    adb devices
    echo ==========û���ҵ������豸����ʹ��adb devices��������Ƿ�����������
    goto END
)

IF NOT %c%==1 (
    SET /a c=0
    FOR /f %%f in ('adb devices^|find /V "devices"^|find "device"') do (
        SET /a c+=1
        ECHO !c!  %%f
    )
    SET /p o=����!c!���豸��������Ҫѡ��ڼ�����
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

ECHO ʹ��%DEV%���а�װ

echo ===========================================================

ECHO ==========��ʼ����װ��ϵͳ����...
adb -s %DEV% remount

IF ERRORLEVEL 1 (
    echo ==========����װ��ϵͳ�����������󣡣���
    goto END
)


::�ж��Ƿ�Ϊandroid5.0
SET /a APK_COUNT=0
SET /a DIR_COUNT=0
FOR /f %%f in ('adb shell ls /system/app^|find ".apk"') do (
	set /a APK_COUNT+=1
)
FOR /f %%f in ('adb shell ls /system/app^|find /V ".apk"') do (
	set /a DIR_COUNT+=1
)
if %DIR_COUNT% EQU %APK_COUNT% (
    echo ==========�޷�ȷ��ϵͳӦ�ð�װ��ʽ������
    goto END
)
SET /a DIR_INSTALL=0
if %DIR_COUNT%  GTR %APK_COUNT% (
    SET /a DIR_INSTALL=1
)


IF EXIST %TEMP_DIR% RD /S /Q %TEMP_DIR%
ECHO ==========��ʼ������ʱ����Ŀ¼...
MD %TEMP_DIR%
IF ERRORLEVEL 1 (
    echo ==========������ʱ����Ŀ¼�������󣡣���
    goto END
)

FOR %%f in (%APKS%) do (
    SET APK_FILE=%%~dpnxf
	IF EXIST %TEMP_DIR%/lib RD /S /Q %TEMP_DIR%/lib
    ECHO ==========��ʼ��ѹ[%%f]...
    start /min /D %TEMP_DIR% /wait jar -xvf "!APK_FILE!" lib
    IF ERRORLEVEL 1 (
        echo ==========��ѹ[%%f]�������󣡣���
        goto END
    )
	adb -s %DEV% shell rm -rf /system/app/%%~nf
	adb -s %DEV% shell rm -rf /system/app/%%~nxf
	IF %DIR_INSTALL%==1 (
	    adb -s %DEV% shell mkdir -p /system/app/%%~nf/lib/arm
	)
	FOR /R %TEMP_DIR%\lib %%k in (*.so) DO (
		ECHO ==========��ʼ�ϴ���̬��[%%~nxk]...
		if %DIR_INSTALL%==0 (
		    adb -s %DEV% push "%%k" /system/lib/%%~nxk
		) else (
		    adb -s %DEV% push "%%k" /system/app/%%~nf/lib/arm/%%~nxk
		)
		IF ERRORLEVEL 1 (
			echo ==========�ϴ���̬��[%%~nxk]�������󣡣���
			goto END
		)
	)
	ECHO ==========��ʼ�ϴ�apk[%%f]...
	if %DIR_INSTALL%==0 (
		adb -s %DEV% push "%%f" /system/app/%%~nxf
	) else (
		adb -s %DEV% push "%%f" /system/app/%%~nf/%%~nxf
	)
    IF ERRORLEVEL 1 (
        echo ==========�ϴ�apk[%%f]�������󣡣���
        goto END
    )
)


ECHO ==========��ʼ����Ŀ¼...
RD /S /Q %TEMP_DIR%


echo ===========================================================
set o=
set /p o=��װ��ɣ���Ҫ����������? (Y/N):
IF NOT DEFINED o goto END
IF /i "%o%"=="Y" goto YES
goto END
:YES
echo ϵͳ�������������Ժ�...
adb reboot
IF ERRORLEVEL 1 (
    echo ==========�������������������ֹ������豸������
    goto END
)


:END
IF EXIST %TEMP_DIR% RD /S /Q %TEMP_DIR%
pause

