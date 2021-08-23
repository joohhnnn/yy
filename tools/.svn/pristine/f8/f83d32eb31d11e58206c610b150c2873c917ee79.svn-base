# -*- coding: utf-8 -*-

#升级自动测试脚本

import os
import sys
import re
import time
import subprocess
import base64
import random
from threading import Timer

#APK名字设置
__APK_OLD_NAME = 'TXZCore_old_0206.apk'
__APK_NEW_NAME = 'TXZCore_new_0206.apk'
__APK_UP_NAME = 'TXZCore_update.apk'


#SD卡工作目录设置
__ENV_SDCARD = '/sdcard/'
__ENV_APKLOADER = '/data/data/com.txznet.txz/shared_prefs/com.txznet.txz.ApkLoader.xml'


#远程设备规则
__RE_REMOTE_DEV = re.compile(r'^\d+\.\d+\.\d+\.\d+(\:\d+)?$')
__RE_OPT_NAME = re.compile(r'([\w\-\.]+)')

#env声明
'''
dev adb设备序列号，可选
tm  启动时间
tag 测试标签，由启动时间生成
dir 工作目录
log 日志文件
cnt_total   测试次数
cnt_success 成功次数
cnt_reboot  重启设备测试次数
cnt_restart 重启进程测试次数
cnt_kill    杀进程测试次数
fail_list   出现失败的时间点列表
test_trace  测试路径
'''

#########################################################################


import ctypes
import sys

TH32CS_SNAPPROCESS = 0x00000002
class PROCESSENTRY32(ctypes.Structure):
    _fields_ = [("dwSize", ctypes.c_ulong),
                 ("cntUsage", ctypes.c_ulong),
                 ("th32ProcessID", ctypes.c_ulong),
                 ("th32DefaultHeapID", ctypes.c_ulong),
                 ("th32ModuleID", ctypes.c_ulong),
                 ("cntThreads", ctypes.c_ulong),
                 ("th32ParentProcessID", ctypes.c_ulong),
                 ("pcPriClassBase", ctypes.c_ulong),
                 ("dwFlags", ctypes.c_ulong),
                 ("szExeFile", ctypes.c_char * 260)]

def getProcList():
    CreateToolhelp32Snapshot = ctypes.windll.kernel32.CreateToolhelp32Snapshot
    Process32First = ctypes.windll.kernel32.Process32First
    Process32Next = ctypes.windll.kernel32.Process32Next
    CloseHandle = ctypes.windll.kernel32.CloseHandle
    hProcessSnap = CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0)
    pe32 = PROCESSENTRY32()
    pe32.dwSize = ctypes.sizeof(PROCESSENTRY32)
    if Process32First(hProcessSnap,ctypes.byref(pe32)) == False:
        print >> sys.stderr, "Failed getting first process."
        return
    while True:
        yield pe32
        if Process32Next(hProcessSnap,ctypes.byref(pe32)) == False:
            break
    CloseHandle(hProcessSnap)

def getChildPid(pid):
    procList = getProcList()
    for proc in procList:
        if proc.th32ParentProcessID == pid:
            yield proc.th32ProcessID

#杀掉进程的进程树
def killPid(pid):
    childList = getChildPid(pid)
    for childPid in childList:
        killPid(childPid)
    handle = ctypes.windll.kernel32.OpenProcess(1, False, pid)
    ctypes.windll.kernel32.TerminateProcess(handle,0)


#美化设备名字
def optDevName(dev):
    return '#'.join(__RE_OPT_NAME.findall(dev))

def delayTime(env, t):
    writeLog(env, '延迟等待: %s......' % t)
    time.sleep(t)


#使用设备准备环境变量
def genEnv(dev=None):
    env = {}
    if dev is not None:
        env['dev'] = dev
    env['tm'] = time.time()
    if dev is None:
        env['tag'] = time.strftime('%Y%m%d_%H%M%S')
    else:
        env['tag'] = optDevName(dev) + '_' + base64.b64encode(dev) + '_' + time.strftime('%Y%m%d_%H%M%S')
    env['dir'] = env['tag']
    os.makedirs(env['dir'])
    env['log'] = open(env['dir'] + '/upgrade.log', 'w')
    env['cnt_total'] = 0
    env['cnt_success'] = 0
    env['cnt_reboot'] = 0
    env['cnt_restart'] = 0
    env['cnt_kill'] = 0
    env['fail_list'] = []
    env['test_trace'] = []
    return env


#日志接口
def writeLog(env, msg):
    dev = env.get('dev', None)
    log = env.get('log', None)
    t = time.time()
    tm = time.strftime('%Y-%m-%d %H:%M:%S', time.localtime(t)) + ('.%03d' % (round(t * 1000)%1000))
    msg = '[%s][%s]%s\n' % (tm, dev, msg)
    sys.stdout.write(msg.decode('utf-8').encode('gb2312'))
    if log is not None:
        log.write(msg)
        log.flush()


#执行命令行
def runCommand(env, cmds, inputs=None, check=None, timeout=None):
    #####writeLog(env, '执行指令: %s' % cmds)
    #return os.system(' '.join(cmds))
    if type(inputs) == type([]):
        inputs = '\n'.join(inputs) + '\n'
    infd =subprocess.PIPE
    if inputs is not None and type(inputs) != type(''):
        infd = inputs
        inputs = None
    p = subprocess.Popen(cmds, stdin=infd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=True, creationflags=subprocess.CREATE_NEW_PROCESS_GROUP)
    if inputs is not None:
        print inputs
    def kp(_env, _p, _timeout):
        writeLog(_env, '执行过程出现超时: %s' % _timeout)
        killPid(_p.pid)
        _p.returncode = None
    if timeout is not None:
        writeLog(env, '任务超时设置: %s' % timeout)
        my_timer = Timer(timeout, kp, [env, p, timeout])
        my_timer.start()
    else:
        my_timer = None
    stdoutdata, stderrdata = p.communicate(inputs)
    #writeLog(env, '任务执行完毕')
    if my_timer is not None:
        my_timer.cancel()
    ret = p.returncode
    #print stdoutdata
    #print stderrdata
    if check is not None:
        content = ''
        if stdoutdata is not None:
            content += stdoutdata
        if stderrdata is not None and stderrdata != '':
            content += stderrdata
            writeLog(env, '执行过程出现错误: %s' % stderrdata)
            #出现设备未连接错误时，重连再执行
            if stderrdata.find('device not found') >= 0:
                 checkConnect(env, False, False)
                 return runCommand(env, cmds, inputs, check, timeout)
        r = check(content)
        if r is not None:
            ret = r
    #####writeLog(env, '执行返回: %s' % ret)
    return ret

#运行adb指令
def runAdbCommand(env, cmds, inputs=None, check=None, timeout=None):
    dev = env.get('dev', None)
    if dev is not None:
        cmds = ['adb', '-s', dev] + cmds
    else:
        cmds = ['adb'] + cmds
    return runCommand(env, cmds, inputs, check, timeout)


#判断文件是否存在
def checkAbdFileExist(env, path):
    def checkExist(content):
        if content.find('No such file or directory') > 0:
            return -1
        return 0
    return runAdbCommand(env, ['shell', 'ls', path], check=checkExist)


#判断指令是否可以使用
def checkAdbCommand(env, cmd):
    def checkExist(content):
        if content.find('not found') > 0:
            return -1
        return 0
    if runAdbCommand(env, ['shell', cmd], check=checkExist) != 0:
        writeLog(env, '>>>命令行%s无法执行' % cmd)
        return -1
    return 0



#确认连接
def checkConnect(env, test, needRoot=True):
    dev = env.get('dev', None)
    n = 1
    if dev is not None and __RE_REMOTE_DEV.match(dev):
        while True:
            writeLog(env, '>>>尝试连接远程设备: %d' % n)
            if runCommand(env, ['adb', 'connect', dev]) == 0:break
            n += 1
            if n > 10 and test: return False
            delayTime(env, 10)
        writeLog(env, '>>>远程设备连接成功')
    else:
        while True:
            writeLog(env, '>>>尝试连接本地设备: %d' % n)
            if runAdbCommand(env, ['shell', 'true']) == 0:break
            n += 1
            if n > 10 and test: return False
            delayTime(env, 10)
        writeLog(env, '>>>本地设备连接成功')
    if not needRoot:
        return True
    writeLog(env, '>>>申请adb的root权限')
    ret = runAdbCommand(env, ['root'])
    if ret != 0 and ret != 1: #ret=1时是：adbd is already running as root
        writeLog(env, '>>>申请adb的root权限返回错误，测试可能会出现异常： %s' % ret)
    if dev is not None and __RE_REMOTE_DEV.match(dev):
        runAdbCommand(env, ['disconnect', dev])
        checkConnect(env, test, False)
    return True
    

#清理安装环境
def clearDevice(env):
    writeLog(env, '>>>清理环境')
    cmds = [
'mount -o rw,remount /system',

'rm -rf /system/priv-app/TXZCore',
'rm -rf /system/app/TXZCore*',
'rm -rf /system/app/TXZCore*.apk',
'rm -rf /system/vendor/operator/app/TXZCore',
'rm -rf /system/lib/libasrfix.so',
'rm -rf /sdcard/txz',

'pm uninstall com.txznet.txz',

'exit',
]
    runAdbCommand(env, ['shell', 'su'], inputs=cmds, timeout=15)
    runAdbCommand(env, ['shell', 'su', 'nobody'], inputs=cmds, timeout=15) #普方达
    runAdbCommand(env, ['shell'], inputs=cmds)
    

#准备环境
def prepareDevice(env):
    clearDevice(env)
    rebootDevice(env)
    writeLog(env, '>>>拷贝安装包')
    if checkAbdFileExist(env, __ENV_SDCARD+__APK_OLD_NAME) != 0:
        runAdbCommand(env, ['push', __APK_OLD_NAME, __ENV_SDCARD])
    if checkAbdFileExist(env, __ENV_SDCARD+__APK_NEW_NAME) != 0:
        runAdbCommand(env, ['push', __APK_NEW_NAME, __ENV_SDCARD])
    

#校验环境
def checkDevice(env):
    writeLog(env, '>>>校验环境')
    if checkAdbCommand(env, 'ls') != 0:return False
    if checkAdbCommand(env, 'ps') != 0:return False
    if checkAdbCommand(env, 'kill') != 0:return False
    if checkAdbCommand(env, 'grep') != 0:return False
    if checkAdbCommand(env, 'pm') != 0:return False
    if checkAdbCommand(env, 'am') != 0:return False
    if checkAdbCommand(env, 'cp') != 0:return False
    if checkAdbCommand(env, 'true') != 0:return False
    if checkAdbCommand(env, 'busybox') != 0:return False
    #if checkAdbCommand(env, 'awk') != 0:return False
    if checkAbdFileExist(env, __ENV_SDCARD+__APK_OLD_NAME) != 0:
        writeLog(env, '>>>校验环境失败，%s不存在' % (__ENV_SDCARD+__APK_OLD_NAME))
        return False
    if checkAbdFileExist(env, __ENV_SDCARD+__APK_NEW_NAME) != 0:
        writeLog(env, '>>>校验环境失败，%s不存在' % (__ENV_SDCARD+__APK_NEW_NAME))
        return False
    return True


#获取最后的上报信息
def getReport(env):
    dev = env.get('dev', None)
    root = env.get('dir', None)
    writeLog(env, '>>>获取上报文件')
    runAdbCommand(env, ['pull', '/sdcard/txz/report', root+'/'])

#重启设备
def rebootDevice(env, test=True):
    dev = env.get('dev', None)
    writeLog(env, '>>>重启设备')
    if test:
        env['cnt_reboot'] += 1
        env['test_trace'].append('rebootDevice')
    runAdbCommand(env, ['reboot'], timeout=10)
    delayTime(env, 45)
    if dev is not None and __RE_REMOTE_DEV.match(dev):
        #需要先断开连接，否则下一步时连接不会断开
        runAdbCommand(env, ['disconnect', dev])
    ret = checkConnect(env, test)
    #某些设备的初始化程序用的是sdkdemo
    runAdbCommand(env, ['shell', 'monkey', '-p', 'com.txznet.sdkdemo', '1'])
    delayTime(env, 30)
    return ret


#重启进程
def restartProcess(env):
    writeLog(env, '>>>开始重启进程')
    env['cnt_restart'] += 1
    env['test_trace'].append('restartProcess')
    runAdbCommand(env, ['shell', 'am', 'broadcast', '-a', 'com.txznet.txz.invoke', '-d', 'txznet://com.txznet.txz/comm.update.restart'])


#杀死进程
def killProcess(env):
    writeLog(env, '>>>开始杀掉进程')
    env['cnt_total'] += 1
    env['test_trace'].append('killProcess')
    runAdbCommand(env, ['shell', "kill `ps|grep txz$|busybox awk '{print $2}'`"])

#尝试一种破坏
def testBroke(env):
    p = random.random()
    if p < 1/3.0:
        rebootDevice(env)
        return
    if p < 2/3.0:
        restartProcess(env)
        return
    killProcess(env)
    

#安装程序
def installApk(env):
    writeLog(env, '>>>安装旧版本')
    runAdbCommand(env, ['shell', 'pm', 'install', '-r', __ENV_SDCARD+__APK_OLD_NAME])
    rebootDevice(env, False)


#升级程序

def upgradeApk(env):
    writeLog(env, '>>>开始执行升级')
    runAdbCommand(env, ['shell', 'cp', __ENV_SDCARD+__APK_NEW_NAME, __ENV_SDCARD+__APK_UP_NAME])
    runAdbCommand(env, ['shell', 'am', 'broadcast', '-a', 'com.txznet.txz.invoke', '-d', 'txznet://com.txznet.txz/comm.update.quickUpgrade?'+__ENV_SDCARD+__APK_UP_NAME])
    #升级发送后等待30s
    delayTime(env, 30)


#回滚程序
def rollbackApk(env):
    writeLog(env, '>>>开始回滚版本')
    runAdbCommand(env, ['shell', 'am', 'broadcast', '-a', 'com.txznet.txz.invoke', '-d', 'txznet://com.txznet.txz/comm.update.rollback'])


#确认是否发生了回滚，当升级包不存在时则表示发生了回滚，cnt大于3次才算失败
def checkRollback(env, cnt=1):
    if cnt > 3:return False
    if cnt > 1:
        #等待5秒再进行回滚检查
        delayTime(env, 5)
    writeLog(env, '>>>校验是否回滚[%d]' % cnt)
    if checkAbdFileExist(env, __ENV_SDCARD+__APK_UP_NAME) != 0:
        writeLog(env, '>>>升级出现回滚[%d]：升级包被删除' % cnt)
        return checkRollback(env, cnt+1)
    def checkXml(content):
        #判断xml的内容中是否有升级包路径
        if content.find(__ENV_SDCARD+__APK_UP_NAME) > 0:
            return 0
        return -1
    if runAdbCommand(env, ['shell', 'cat', __ENV_APKLOADER], check=checkXml) != 0:
        writeLog(env, '>>>升级出现回滚[%d]：升级配置丢失' % cnt)
        return checkRollback(env, cnt+1)
    writeLog(env, '>>>升级未发生回滚')
    return True

#开始测试一个设备
def beginTest(dev=None):
    env = genEnv(dev)
    while True:
        writeLog(env, '>>>重置环境开始测试')
        env['test_trace'] = []
        if not checkConnect(env, False):
            writeLog(env, '>>>致命错误：设备环境无法连接，测试无法进行')
            return
        prepareDevice(env)
        if not checkDevice(env):
            writeLog(env, '>>>致命错误：设备环境有问题，测试无法进行')
            return
        installApk(env)
        #安装重启后等待1分钟
        delayTime(env, 10)
        upgradeApk(env)
        if not checkRollback(env):
            writeLog(env, '>>>致命错误：升级失败，测试无法进行')
            continue
        #开始真正的测试循环
        while True:
            env['cnt_total'] += 1
            writeLog(env, '>>>开始第%d次测试' % env['cnt_total'])
            testcases = [(rebootDevice, 15, 120), (restartProcess, 15, 120), (killProcess, 15, 120), (testBroke, 50, 120)]
            #n次重启设备测试
            #n次重启进程测试
            #n次杀进程测试
            #n次测试破坏
            fail = False
            for funcBroke, maxCnt, delayTimeCount in testcases:
                cnt = random.randint(10, maxCnt)
                for i in range(cnt):
                    #开始尝试破坏
                    t1 = time.time()
                    funcBroke(env)
                    t2 = time.time()
                    t = delayTimeCount - (t2-t1)
                    if t <= 0:
                        t = 10
                    delayTime(env, t)
                    if not checkRollback(env):
                        tm = time.strftime('%Y-%m-%d %H:%M:%S')
                        env['fail_list'].append(tm)
                        writeLog(env, '>>>出现回滚错误：成功次数%d，失败次数%d' % (env['cnt_success'], len(env['fail_list'])))
                        writeLog(env, '>>>异常路径：%s' % (','.join(env['test_trace'])))
                        getReport(env)
                        fail = True
                        break
                    env['cnt_success'] += 1
                    writeLog(env, '>>>测试通过：成功次数%d，失败次数%d' % (env['cnt_success'], len(env['fail_list'])))
                if fail:break
            if fail:break

#读取设备列表
def readDeviceList():
    #先执行一遍adb devices
    os.system('adb devices')
    ret = []
    p = subprocess.Popen(['adb', 'devices'], stdout=subprocess.PIPE)
    outstr, errstr = p.communicate()
    for dev in outstr.splitlines()[1:]:
        dev = dev.strip()
        n = len(dev)
        while n > 0:
            n -= 1
            if dev[n] == ' ' or dev[n] == '\t':
                break
        if n <= 0:continue
        dev = dev[:n].strip()
        ret.append(dev)
    return ret

#入口函数
def mainProc():
    if len(sys.argv) > 1:
        if sys.argv[1] != 'clean':
            beginTest(sys.argv[1])
            return
        for dev in readDeviceList():
            print '============begin clean: ' + dev
            p = subprocess.Popen(['adb', '-s', dev, 'shell', 'rm', '/sdcard/*.apk'])
            p.wait()
        print '============clean all over'
        return
    plist = []
    for dev in readDeviceList():
        print '============begin test: ' + dev 
        p = subprocess.Popen([sys.argv[0], dev], stdout=sys.stdout, stderr=sys.stderr, shell=True)
        plist.append(p)
        time.sleep(1)
    while True:
        end = 0
        for p in plist:
            if p.poll() is None:
                    break
            end += 1
        if end == len(plist):
            print '============test all over'
            return
        #等待测试进程结束
        time.sleep(10)
        
if __name__ == '__main__':
    mainProc()
    

    
