@echo off

cls

setlocal EnableDelayedExpansion


echo ==========同行者日志拉取脚本 by bihongpi==========
echo ===========================================================

set TIME_STR=%DATE:~0,4%%DATE:~5,2%%DATE:~8,2%_%TIME:~0,2%%TIME:~3,2%%TIME:~6,2%

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

ECHO 使用%DEV%拉取日志信息

set SDCARD=%1
if "%1"=="" set SDCARD=/sdcard

set WORK_DIR=__TXZ_LOGS_%TIME_STR%

RD /S /Q %WORK_DIR%

MD "%WORK_DIR%"
MD "%WORK_DIR%\log"
MD "%WORK_DIR%\report"

echo ==========开始拉取日志[text_all]
adb -s %DEV% pull "%SDCARD%/txz/log/text_all" "%WORK_DIR%\log"
copy "%WORK_DIR%\log\text_all" /B "%WORK_DIR%\merge.log" /B
for /l %%i in (1 1 9) do (
    echo ==========开始拉取日志[text_all_%%i]
    adb -s %DEV% pull "%SDCARD%/txz/log/text_all_%%i" "%WORK_DIR%\log"
	IF ERRORLEVEL 0 (
	    IF EXIST "%WORK_DIR%\log\text_all_%%i" (
			echo ==========开始合并日志[text_all_%%i]
			rename "%WORK_DIR%\merge.log" merge.tmp
			copy "%WORK_DIR%\log\text_all_%%i" /B + "%WORK_DIR%\merge.tmp" /B "%WORK_DIR%\merge.log" /B
			del /S /Q "%WORK_DIR%\merge.tmp"
		)
	)
)

echo ==========开始拉取错误报告[report]
adb -s %DEV% pull "%SDCARD%/txz/report" "%WORK_DIR%\report"

echo ===========================================================
echo ==========日志拉取完成：%WORK_DIR%


:END
pause