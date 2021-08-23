# -*- coding: gbk -*-
import os
import sys
import time
import hashlib

#当前目录
CUR_DIR = os.path.abspath(os.getcwd())

#SDK项目配置
SDK_PRJ_BASE_DIR = r'..\..\rearview_mirror'
SDK_PRJ = {
    'TXZComm': 'TXZ_SDK',
    '':''
}
#APP项目配置
APP_PRJ_BASE_DIR = SDK_PRJ_BASE_DIR
APP_PRJ = {
    'TXZCore':'TXZCore',
    'TXZRecord':'TXZRecord',
    'TXZWebchat':'TXZWebchat',
    'TXZMusic':'TXZMusic',
    'TXZNav':'TXZGDNav',
    'TXZFeedback':'TXZFeedback',
    '':''
}
#项目配置，选择打包哪些项目
PRJ_NAME = ['0004.诺威达']
PRJ_NAME = None #取消注释则打包所有工程

def printInfo(info):
    sys.stderr.write('%s\n' % str(info))

def genWin32Path(p):
    return os.path.abspath(p).replace('/', '\\')

def makeDirs(d):
    try:
        os.makedirs(d)
    except:
        pass

def rmDir(d):
    os.system('rd /S /Q "%s"' % genWin32Path(d))

def copyFile(src, dst):
    runCommand('copy /B /Y "%s" "%s"' % (genWin32Path(src), genWin32Path(dst)))

def copyDir(src, dst):
    runCommand('xcopy /S /E /H /R /Y "%s" "%s"' % (genWin32Path(src), genWin32Path(dst)))

def removeFile(p):
    os.remove(p)

def md5Str(s):
    return hashlib.new("md5", s).hexdigest()

def runCommand(cmd):
    #printInfo(os.getcwd())
    #printInfo(cmd)
    return os.system(cmd)

def recoveBackups():
    os.chdir(CUR_DIR)
    global BAK_PRJS #备份项目列表
    for p in BAK_PRJS:
        bak_root = '~bak_'+md5Str(p)
        for file_name in BAK_PRJS[p]:
            dst_path = os.path.join(p, file_name)
            printInfo('开始恢复备份' + dst_path)
            rmDir(dst_path)
            makeDirs(dst_path)
            copyDir(os.path.join(bak_root, file_name), dst_path)
        rmDir(bak_root)
    BAK_PRJS = {}

def procErr(err):
    sys.stderr.write('!!!!!!发生致命错误：' + err)
    recoveBackups()
    sys.exit(-1)

def createReleaseDir():
    global OUT_DIR
    OUT_DIR = os.path.abspath('release_apks_' + time.strftime('%Y%m%d'))
    rmDir(OUT_DIR)
    makeDirs(OUT_DIR + '/sdk')
    makeDirs(OUT_DIR + '/doc')
    makeDirs(OUT_DIR + '/mapping')
    makeDirs(OUT_DIR + '/log')

def clearSDK():
    for p in SDK_PRJ:
        if p == '':continue
        os.chdir(SDK_PRJ_BASE_DIR)
        os.chdir(p)
        printInfo('开始清理SDK：' + p)
        if runCommand(r'ant clean') != 0: procErr('清理%s发生异常' % p)
        os.chdir(CUR_DIR)

def releaseSDK():
    global SDK_FILES #SDK列表
    for p in SDK_PRJ:
        if p == '':continue
        os.chdir(SDK_PRJ_BASE_DIR)
        os.chdir(p)
        printInfo('开始编译SDK：' + p)
        if runCommand(r'ant release') != 0: procErr('编译%s发生异常' % p)
        os.chdir(CUR_DIR)
        sdk = os.path.join(SDK_PRJ_BASE_DIR, p, 'bin', SDK_PRJ[p]+'.jar')
        sdk_out = os.path.abspath(os.path.join(OUT_DIR, 'sdk', SDK_PRJ[p]+'.jar'))
        SDK_FILES[SDK_PRJ[p]+'.jar'] = sdk_out
        copyFile(sdk, sdk_out)
        copyFile(os.path.join(SDK_PRJ_BASE_DIR, p, 'doc', SDK_PRJ[p]+'.zip'), os.path.join(OUT_DIR, 'doc', SDK_PRJ[p]+'.zip'))
        copyFile(os.path.join(SDK_PRJ_BASE_DIR, p, 'bin', 'proguard_mapping.txt'), os.path.join(OUT_DIR, 'mapping', p+'.txt'))

def clearApp():
    for p in APP_PRJ:
        if p == '':continue
        os.chdir(SDK_PRJ_BASE_DIR)
        os.chdir(APP_PRJ[p])
        printInfo('开始清理App：' + p)
        if runCommand(r'ant clean') != 0: procErr('清理%s发生异常' % p)
        os.chdir(CUR_DIR)

def releaseApp():
    for p in APP_PRJ:
        if p == '':continue
        os.chdir(SDK_PRJ_BASE_DIR)
        os.chdir(APP_PRJ[p])
        printInfo('开始编译App：' + p)
        if runCommand(r'ant release') != 0: procErr('编译%s发生异常' % p)
        os.chdir(CUR_DIR)
        copyFile(os.path.join(SDK_PRJ_BASE_DIR, APP_PRJ[p], 'bin', p+'-release.apk'), os.path.join(OUT_DIR, p+'.apk'))
        copyFile(os.path.join(SDK_PRJ_BASE_DIR, APP_PRJ[p], 'bin', 'proguard', 'mapping.txt'), os.path.join(OUT_DIR, 'mapping', p+'.txt'))

def readPrjConfig(fp):
    cfg_data = open(fp).read()
    try:
        cfg_data = cfg_data.decode('utf-8')
    except:
        cfg_data = cfg_data.decode('gbk')
    cfg_data = cfg_data.encode('gbk')
    return eval(cfg_data)

def preProcPrjList():
    global PRJ_LIST #项目列表
    global BAK_PRJS #备份项目列表
    printInfo('开始清理工程列表')
    for p in PRJ_LIST:
        if p == '': continue
        cfg_path = os.path.abspath(os.path.join(CUR_DIR, 'config', p))
        for d in os.listdir(cfg_path):
            if d == '.' or d == '..' or d[0] == '_': continue
            path = os.path.join(cfg_path, d)
            if not os.path.isdir(path):continue
            cfg = readPrjConfig(os.path.join(cfg_path, d+'.cfg'))
            dir_name = os.path.dirname(os.path.abspath(cfg['path']))
            file_name = os.path.basename(os.path.abspath(cfg['path']))
            lst = BAK_PRJS.get(dir_name, [])
            printInfo(p+'开始清理' + d)
            if file_name in lst:continue
            lst.append(file_name)
            bak_path = os.path.join('~bak_' + md5Str(dir_name), file_name)
            makeDirs(bak_path)
            copyDir(cfg['path'], bak_path)
            BAK_PRJS[dir_name] = lst
            if cfg['apk']:
                #拷贝编译脚本
                os.chdir(cfg['path'])
                if runCommand(r'ant clean') != 0: procErr(p + '清理' + d + '发生异常')
                os.chdir(CUR_DIR)
        
def procPrjList():
    printInfo('开始处理工程列表')
    global PRJ_LIST #项目列表
    global OUT_DIR #输出目录
    for p in PRJ_LIST:
        if p == '': continue
        cfg_path = os.path.abspath(os.path.join(CUR_DIR, 'config', p))
        prj_dir = os.path.abspath(os.path.join(OUT_DIR, p))
        #开始预处理
        runCommand('%s %s %s' % (os.path.join(cfg_path, '_pre_process.bat'), cfg_path, prj_dir))
        for d in os.listdir(cfg_path):
            if d == '.' or d == '..' or d[0] == '_': continue
            path = os.path.join(cfg_path, d)
            if not os.path.isdir(path):continue
            makeDirs(prj_dir)
            cfg = readPrjConfig(os.path.join(cfg_path, d+'.cfg'))
            copyDir(path, os.path.join(cfg['path']))
            for sdk in SDK_FILES:
                sdk_path = os.path.join(cfg['path'], 'libs', sdk)
                if os.path.isfile(sdk_path):
                    printInfo(p+'的'+d+'开始替换'+sdk)
                    copyFile(SDK_FILES[sdk], sdk_path)
            if cfg['zip']:
                printInfo(p+'开始打包'+d)
                rmDir(os.path.join(cfg['path'], 'proguard'))
                rmDir(os.path.join(cfg['path'], 'bin'))
                rmDir(os.path.join(cfg['path'], 'gen'))
                os.chdir(os.path.join(cfg['path'], '..'))
                if runCommand('jar cvf "%s" %s' % (genWin32Path(os.path.join(prj_dir, d+'.zip')), os.path.basename(cfg['path']))) != 0: procErr(p + '源码打包' + d + '发生异常')
                os.chdir(CUR_DIR)
            if cfg['apk']:
                printInfo(p+'开始编译'+d)
                os.chdir(cfg['path'])
                if runCommand(r'ant release') != 0: procErr(p + '编译' + d + '发生异常')
                if cfg['zip']:
                    copyFile(os.path.join('bin', 'TXZ-release-unsigned.apk'), os.path.join(prj_dir, d+'.apk'))
                else:
                    copyFile(os.path.join('bin', cfg.get('name', d)+'-release.apk'), os.path.join(prj_dir, d+'.apk'))
                copyFile(os.path.join('bin', 'proguard', 'mapping.txt'), os.path.join(OUT_DIR, 'mapping', p+'_'+d+'.txt'))
                os.chdir(CUR_DIR)
        #开始后处理脚本
        runCommand('%s %s %s' % (os.path.join(cfg_path, '_suf_process.bat'), cfg_path, prj_dir))


def clearAll():
    printInfo('开始清理...')
    clearSDK()
    clearApp()

def releaseAll():
    printInfo('开始发布...')
    releaseSDK()
    releaseApp()

def mainProc():
    begin_time = time.time()
    
    global OUT_DIR #输出目录
    global SDK_FILES #SDK列表
    global PRJ_LIST #项目列表
    global BAK_PRJS #备份项目列表
    SDK_FILES = {}
    BAK_PRJS = {}
    if PRJ_NAME is not None:
        if type(PRJ_NAME) == type([]):
            PRJ_LIST = PRJ_NAME
        else:
            PRJ_LIST = [PRJ_NAME]
    else:
        PRJ_LIST = []
        for d in os.listdir(os.path.join(CUR_DIR, 'config')):
            if d == '.' or d == '..' or d[0] == '_': continue
            if os.path.isdir(os.path.join(CUR_DIR, 'config', d)):
                PRJ_LIST.append(d)
    printInfo('当前工作目录: ' + CUR_DIR)

    for d in os.listdir('.'):
        if len(d)>5 and d[:5] == '~bak_':
            rmDir(d)
    
    createReleaseDir()
    clearAll()
    preProcPrjList()
    releaseAll()
    procPrjList()
    recoveBackups()

    end_time = time.time()
    printInfo('全部处理完成，供花费时间：' + str(end_time-begin_time) + '秒')


if __name__ == '__main__':
    mainProc()



