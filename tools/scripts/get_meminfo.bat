
del /S /Q *.log *.tmp
rd /S /Q tasks
rd /S /Q mems
rd /S /Q fds

adb root
adb remount

mkdir tasks
mkdir mems
mkdir fds

:BEGIN

adb shell "ps |grep 'com.txznet.txz$'|busybox awk '{printf $2}'" >pid.tmp
for /f "tokens=* delims=" %%i in (pid.tmp) do (set PID=%%i)

echo PID=%PID%


REM 记录整体状态信息
adb shell date +%%Y-%%m-%%d_%%H:%%M:%%S >>status.log
echo "====================" >>status.log
>>status.log set /p="ThreadCount=" <nul
adb shell "ls /proc/%PID%/task|busybox wc -l">>status.log
>>status.log set /p="FdCount=" <nul
adb shell "ls /proc/%PID%/fd|busybox wc -l">>status.log
echo "MEM_INFO: ">>status.log
adb shell "dumpsys meminfo com.txznet.txz |grep Binder && dumpsys meminfo com.txznet.txz |grep View && dumpsys meminfo | grep com.txznet.txz" >>status.log

REM 记录线程信息
adb shell date +%%Y%%m%%d_%%H%%M%%S >time.tmp
for /f "tokens=* delims=" %%i in (time.tmp) do (set CURTIME=%%i)
adb shell "ls /proc/%PID%/task | busybox awk '{print $1,"."0}'" > task.tmp
echo "====================" >>tasks\%CURTIME%.log
for /f "tokens=1 delims= " %%i in (task.tmp) do (
adb shell cat /proc/%PID%/task/%%i/comm >> tasks\%CURTIME%.log
)

REM 记录FD信息
adb shell date +%%Y%%m%%d_%%H%%M%%S >time.tmp
for /f "tokens=* delims=" %%i in (time.tmp) do (set CURTIME=%%i)
echo "====================" >>fds\%CURTIME%.log
adb shell ls -al /proc/%PID%/fd >> fds\%CURTIME%.log

REM 记录内存信息
adb shell date +%%Y%%m%%d_%%H%%M%%S >time.tmp
for /f "tokens=* delims=" %%i in (time.tmp) do (set CURTIME=%%i)
echo "====================" >>mems\%CURTIME%.log
adb shell dumpsys meminfo com.txznet.txz >>mems\%CURTIME%.log

REM 详细内存信息
adb shell date +%%Y-%%m-%%d_%%H:%%M:%%S >>sysmem.log
echo "====================" >>sysmem.log
adb shell "dumpsys meminfo| grep com.txznet.txz" >>sysmem.log

@ping 1.1.1.1 -n 1 -w 1000


goto BEGIN
