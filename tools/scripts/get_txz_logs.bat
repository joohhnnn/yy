��
@echo off

cls

setlocal EnableDelayedExpansion


echo ==========ͬ������־��ȡ�ű� by bihongpi==========
echo ===========================================================

set TIME_STR=%DATE:~0,4%%DATE:~5,2%%DATE:~8,2%_%TIME:~0,2%%TIME:~3,2%%TIME:~6,2%

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

ECHO ʹ��%DEV%��ȡ��־��Ϣ

set SDCARD=%1
if "%1"=="" set SDCARD=/sdcard

set WORK_DIR=__TXZ_LOGS_%TIME_STR%

RD /S /Q %WORK_DIR%

MD "%WORK_DIR%"
MD "%WORK_DIR%\log"
MD "%WORK_DIR%\report"

echo ==========��ʼ��ȡ��־[text_all]
adb -s %DEV% pull "%SDCARD%/txz/log/text_all" "%WORK_DIR%\log"
copy "%WORK_DIR%\log\text_all" /B "%WORK_DIR%\merge.log" /B
for /l %%i in (1 1 9) do (
    echo ==========��ʼ��ȡ��־[text_all_%%i]
    adb -s %DEV% pull "%SDCARD%/txz/log/text_all_%%i" "%WORK_DIR%\log"
	IF ERRORLEVEL 0 (
	    IF EXIST "%WORK_DIR%\log\text_all_%%i" (
			echo ==========��ʼ�ϲ���־[text_all_%%i]
			rename "%WORK_DIR%\merge.log" merge.tmp
			copy "%WORK_DIR%\log\text_all_%%i" /B + "%WORK_DIR%\merge.tmp" /B "%WORK_DIR%\merge.log" /B
			del /S /Q "%WORK_DIR%\merge.tmp"
		)
	)
)

echo ==========��ʼ��ȡ���󱨸�[report]
adb -s %DEV% pull "%SDCARD%/txz/report" "%WORK_DIR%\report"

echo ===========================================================
echo ==========��־��ȡ��ɣ�%WORK_DIR%


:END
pause