REM %1 ��Ŀ����Ŀ¼����·��
REM %2 ��Ŀ���Ŀ¼����·��

echo %1
echo %2

xcopy /S /R /Y %1\_raw\* %2\

copy /B /Y %2\..\TXZ*.apk %2\


