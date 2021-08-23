::set "ANT_OPTS=-Xms1024m -Xmx4096m"
::set "JAVA_OPTS=-Xms1024m -Xmx4096m"
::set "javaOpts=-Xms1024m -Xmx4096m"

..\TXZComm\gen_dict.py
call ant clean
call ant release
pause


