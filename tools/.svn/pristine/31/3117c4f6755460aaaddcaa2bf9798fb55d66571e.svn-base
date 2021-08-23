@echo off
setlocal EnableDelayedExpansion
echo.正在查找sdcard...
set "pre=/mnt/sdcard/txz/"
set "sd="
set "input="
set "return="
for /f "delims=" %%a in ('adb shell cat %pre%') do (
call:strstr "%%a" "Is a directory" 14
:retry
if "!return!" == "0" (
set /p sdcard=未找到sdcard,请手动输入sdcard中txz文件夹的位置,输入0退出:
if "!sdcard!" == "0" (
goto end
)
if not "!sdcard:~-1!" == "/" (
set "sdcard=!sdcard!/"
)
for /f "delims=" %%b in ('adb shell cat !sdcard!') do (
call:strstr "%%b" "Is a directory" 14
if "!return!" == "0" (
goto retry
) else (
set "sd=!sdcard!"
)
)
) else (
set "sd=%pre%"
)
)
echo.查找成功,sdcrad位置为：%sd%
:do
echo.********************************
echo.*  请输入序号执行对应操作      *
echo.*  1、打开测试日志(2+3)        *
echo.*  2、打开同行者日志           *
echo.*  3、打开native堆栈信息       *
echo.*  4、打开云之声引擎日志(2.0)  *
echo.*  5、打开云之声引擎日志(1.0)  *
echo.*  6、打开保存唤醒的原始PCM    *
echo.*  7、禁用远程tts工具          *
echo.*  8、禁用远程设置资源         *
echo.*  9、打开GPS信息日志          *
echo.*  10、打开导航广播数据log     *
echo.*  100、开启所有               *
echo.*  0、退出                     *
echo.*  对应负数为关闭              *
echo.********************************
set /p input=
if "%input%" == "1" (
	adb shell "echo \"\" > %sd%log_enable_file"
	adb shell "am broadcast -a com.txznet.txz.invoke -d txznet://com.txznet.txz/comm.log.setConsoleLogLevel?2" > null
	adb shell "echo \"\" > %sd%call_stack_enable"
echo.执行成功,请手动重启TXZ
)
if "%input%" == "-1" (
	adb shell rm %sd%log_enable_file > null
	adb shell "am broadcast -a com.txznet.txz.invoke -d txznet://com.txznet.txz/comm.log.setConsoleLogLevel?6" > null
	adb shell rm %sd%call_stack_enable > null
echo.执行成功,请手动重启TXZ
)
if "%input%" == "2" (
	adb shell "echo \"\" > %sd%log_enable_file"
	adb shell "am broadcast -a com.txznet.txz.invoke -d txznet://com.txznet.txz/comm.log.setConsoleLogLevel?2" > null
echo.执行成功,请手动重启TXZ
)
if "%input%" == "-2" (
	adb shell rm %sd%log_enable_file > null
	adb shell "am broadcast -a com.txznet.txz.invoke -d txznet://com.txznet.txz/comm.log.setConsoleLogLevel?6" > null
echo.执行成功,请手动重启TXZ
)
if "%input%" == "3" (
	adb shell "echo \"\" > %sd%call_stack_enable"
echo.执行成功,请手动重启TXZ
)
if "%input%" == "-3" (
	adb shell rm %sd%call_stack_enable > null
echo.执行成功,请手动重启TXZ
)
if "%input%" == "4" (
	adb shell "echo \"\" > %sd%yzs_log.debug"
echo.执行成功,请手动重启TXZ
)
if "%input%" == "-4" (
	adb shell rm %sd%yzs_log.debug > null
echo.执行成功,请手动重启TXZ
)
if "%input%" == "5" (
	adb shell "echo \"\" > %sd%txz_abc1234321.debug"
echo.执行成功,请手动重启TXZ
)
if "%input%" == "-5" (
	adb shell rm %sd%txz_abc1234321.debug > null
echo.执行成功,请手动重启TXZ
)
if "%input%" == "6" (
	adb shell "echo \"\" > %sd%pcm_enable.debug"
echo.执行成功,请手动重启TXZ
)
if "%input%" == "-6" (
	adb shell rm %sd%pcm_enable.debug > null
echo.执行成功,请手动重启TXZ
)
if "%input%" == "7" (
	adb shell "echo \"\" > %sd%disable_remote_tts_tool.debug"
echo.执行成功,请手动重启TXZ
)
if "%input%" == "-7" (
	adb shell rm %sd%disable_remote_tts_tool.debug > null
echo.执行成功,请手动重启TXZ
)
if "%input%" == "8" (
	adb shell "echo \"\" > %sd%disable_remote_set_res.debug"
echo.执行成功,请手动重启TXZ
)
if "%input%" == "-8" (
	adb shell rm %sd%disable_remote_set_res.debug > null
echo.执行成功,请手动重启TXZ
)
if "%input%" == "9" (
	adb shell "echo \"\" > %sd%ENABLE_TRACE_GPS.debug"
echo.执行成功,请手动重启TXZ
)
if "%input%" == "-9" (
	adb shell rm %sd%ENABLE_TRACE_GPS.debug > null
echo.执行成功,请手动重启TXZ
)
if "%input%" == "10" (
	adb shell "echo \"\" > %sd%ENABLE_TRACE_NAV_INFO.debug"
echo.执行成功,请手动重启TXZ
)
if "%input%" == "-10" (
	adb shell rm %sd%ENABLE_TRACE_NAV_INFO.debug > null
echo.执行成功,请手动重启TXZ
)
if "%input%" == "100" (
	adb shell "echo \"\" > %sd%log_enable_file"
	adb shell "am broadcast -a com.txznet.txz.invoke -d txznet://com.txznet.txz/comm.log.setConsoleLogLevel?2" > null
	adb shell "echo \"\" > %sd%call_stack_enable"
	adb shell "echo \"\" > %sd%ENABLE_TRACE_GPS.debug"
	adb shell "echo \"\" > %sd%yzs_log.debug"
	adb shell "echo \"\" > %sd%txz_abc1234321.debug"
	adb shell "echo \"\" > %sd%pcm_enable.debug"
	adb shell "echo \"\" > %sd%disable_remote_tts_tool.debug"
	adb shell "echo \"\" > %sd%disable_remote_set_res.debug"
	adb shell "echo \"\" > %sd%ENABLE_TRACE_NAV_INFO.debug"
echo.执行成功,请手动重启TXZ
)
if "%input%" == "-100" (
	adb shell rm %sd%log_enable_file > null
	adb shell "am broadcast -a com.txznet.txz.invoke -d txznet://com.txznet.txz/comm.log.setConsoleLogLevel?6" > null
	adb shell rm %sd%ENABLE_TRACE_GPS.debug > null
	adb shell rm %sd%yzs_log.debug > null
	adb shell rm %sd%txz_abc1234321.debug > null
	adb shell rm %sd%pcm_enable.debug > null
	adb shell rm %sd%disable_remote_tts_tool.debug > null
	adb shell rm %sd%disable_remote_set_res.debug > null
	adb shell rm %sd%ENABLE_TRACE_NAV_INFO.debug > null
	adb shell rm %sd%call_stack_enable > null
echo.执行成功,请手动重启TXZ
)
if "%input%" == "0" (
goto end
)
echo.&pause
goto do
:end
echo.&goto:eof

:strstr
set str1=%~1
set str2=%~2
rem 注意，这里是区分大小写的！
set str=%str1%
rem 复制字符串，用来截短，而不影响源字符串
:next
if not "%str%"=="" (
set /a num+=1
if "!str:~0,%~3!"=="%str2%" goto last
rem 比较首字符是否为要求的字符，如果是则跳出循环
set str=%str:~1%
goto next
)
set /a num=0
rem 没有找到字符时，将num置零
:last
set "return=%num%"
rem echo '%str2%' in "%str1%" = %num%
goto:eof