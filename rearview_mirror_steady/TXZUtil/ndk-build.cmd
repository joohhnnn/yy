@echo off
if "%1"=="clean" (
	echo pre clean...
	call jni\tools\compile\run_python_script.bat jni\tools\compile\clean_cpp.py
) else (
	echo pre process...
	call jni\tools\compile\run_python_script.bat jni\tools\compile\pre_cpp.py
)

call %NDK_HOME%/ndk-build %*

if "%1"=="clean" (
	echo suf clean...
) else (
	echo suf process...
	call jni\tools\compile\run_python_script.bat jni\tools\compile\suf_cpp.py
)