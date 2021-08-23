@echo off

setlocal EnableDelayedExpansion

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
	adb %*
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

::ECHO 使用%DEV%操作

adb -s %DEV% %*

:END
