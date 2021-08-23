if exist old_src goto st0
if not exist old_src goto st1

:st0
del /S /Q old_src
rd /S /Q old_src

:st1
md old_src

python copyMergedSource.py old_src src

start "" "C:\Program Files (x86)\Beyond Compare 3\BCompare.exe" old_src  src

pause









