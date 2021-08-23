from txz_compile_tool import *


def MoveSoFile(src, dst):
    os.system('copy /B /Y "%s" "%s"' % (os.path.join(TXZ_GENERAL_DIR, src).replace('/', "\\"), dst.replace('/', "\\")))

def Main():
    pass
    #MoveSoFile('jni/so3rd/libtxzComm.so', 'libs/armeabi/libtxzComm.so')

if __name__ == '__main__':
    Main()
