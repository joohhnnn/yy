from txz_compile_tool import *


def CleanPbsrc():
    #RemoveDir('pbsrc')
    pass

def CleanSo():
    RemoveFile('libs/armeabi/libtxzComm.so')
    pass

def CleanVersionFile():
    RemoveFile(JAVA_VERSION_FILE)

def Main():
    pass
    #CleanPbsrc()
    CleanVersionFile()
    ClearDexFiles()

if __name__ == '__main__':
    Main()
