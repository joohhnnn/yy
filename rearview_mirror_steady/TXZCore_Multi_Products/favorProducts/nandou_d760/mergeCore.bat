copy /B /Y src\WakeupManager.java   ..\..\src\com\txznet\txz\module\wakeup\WakeupManager.java

goto s2
if exist ..\..\src\com\unisound\common goto s1 
md ..\..\src\com\unisound\common
:s1
copy /B /Y src\aa.java   ..\..\src\com\unisound\common\aa.java

:s2

pause









