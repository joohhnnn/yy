import os
import sys
import subprocess

TXZ_TOOLS_DIR = '../../tools'

JAVA_VERSION_FILE = 'src/com/txznet/txz/module/version/TXZVersion.java'

def ExitWithError(s):
    sys.stderr.write(s + '\n')
    os._exit(-1)

def RemoveDir(d):
    print 'begin remove directory [%s]' % d
    os.system(r'rmdir /S /Q "%s"' % d.replace('/', "\\"))


def RemoveFile(f):
    print 'begin remove file [%s]' % f
    os.system(r'del /F /Q "%s"' % f.replace('/', "\\"))


def CopyFileWithTimeCompare(src, dst):
    ts = 0
    td = 0
    if os.path.isfile(src):
        ts = os.stat(src).st_mtime
    if os.path.isfile(dst):
        td = os.stat(dst).st_mtime
    if td == 0 or td < ts:
        print 'begin copy[%s] to [%s]' % (src, dst)
        os.system('copy /B /Y "%s" "%s"' % (src.replace('/', "\\"), dst.replace('/', "\\")))
    else:
        print 'noneed copy[%s]' % src


def DirData(d):
    r = ''
    for f in os.listdir(d):
        #print f
        if f == '.' or f == '..': continue
        fp = os.path.join(d,f)
        if os.path.isdir(fp):
            r += DirData(fp) + '\0'
            continue
        if fp.endswith('.hash'):
            continue
        r += open(fp).read() + '\0'
    return r, t

def DirHash(d):
    h = hashlib.md5(DirData(d)).hexdigest()
    #print h
    return h

def RunCommand(cmd):
    #print cmd
    d = os.popen(cmd).readlines()
    d = ''.join(d)
    #print d
    return d


def RunCommandAtGeneral(cmd):
    d = os.getcwd()
    os.chdir(TXZ_GENERAL_DIR)
    os.system(cmd)
    os.chdir(d)

def RunSubprocess(cmds, d):
    return subprocess.Popen(cmds, cwd=d).wait()


def GetSvnVersion(d='.'):
    svninfo = RunCommand(('%s info "%s"' % (os.path.join(TXZ_TOOLS_DIR, r'sliksvn\svn.exe'), d)).replace('/', "\\"))
    for s in svninfo.splitlines():
        s = s.strip()
        if s.startswith('Revision:'):
            svn_ver = s.split(':')[-1].strip()
            print '[%s] svn version number[%s]' % (d, svn_ver)
            return int(svn_ver)
    sys.stderr.write("get [%s] svn info error\n" % d)
    return -1


def ClearDexFiles():
    os.system(r'rmdir /S /Q dex_tmp')
    os.system(r'del /S /Q libs\txz_gen.jar')
    os.system(r'del /S /Q txz_libs\txz_gen.jar')
    os.system(r'del /S /Q libs/check_jars.pyo')

def CheckDexFiles():
    if not os.path.isfile('libs/txz_gen.jar'):
        print 'missing libs/txz_gen.jar'
        return False
    if not os.path.isfile('txz_libs/txz_gen.jar'):
        print 'missing txz_libs/txz_gen.jar'
        return False
    if not os.path.isfile('libs/check_jars.pyo'):
        print 'missing libs/check_jars.pyo'
        return False
    try:
        fp = open('libs/check_jars.pyo', 'rb')
        hash_list = eval(fp.read())
        fp.close()
    except:
        print 'read check list failed'
        return False
    check_list = ['libs/txz_gen.jar', 'txz_libs/txz_gen.jar']
    for f in os.listdir('txz_libs'):
        if not f.endswith('.jar'): continue
        if f == 'txz_gen.jar': continue
        check_list.append('txz_libs/' + f)
    if len(check_list) != len(hash_list):
        print 'nomatch check file list'
        return False
    for f in check_list:
        t = os.stat(f).st_mtime
        l = os.path.getsize(f)
        r = hash_list.get(f, None)
        if r is None:
            print 'missing file hash: ' + f
            return False
        if int(t) != r[0]:
            print 'nomatch file [%s] mtime: [%d/%d]' % (f, int(t), r[0])
            return False
        if l != r[1]:
            print 'nomatch file [%s] length: [%d/%d]' % (f, l, r[1])
            return False
    return True

def GenDexFiles():
    os.system(r'rmdir /S /Q dex_tmp')
    try:
        if CheckDexFiles():
            print 'no need gen jars again'
            return
    except:
        print 'check file hash except'
    #os.system('pause')
    ClearDexFiles()
    #todo check jar files increase compile speed
    try:
        os.makedirs('dex_tmp')
    except:
        pass
    hash_list = {}
    t = 0
    for f in os.listdir('txz_libs'):
        if not f.endswith('.jar'): continue
        if f == 'txz_gen.jar': continue
        if RunSubprocess(['jar', 'xf', '../txz_libs/'+f], './dex_tmp') != 0:
            ExitWithError('unzip library %s error!!!' % f)
        tt = os.stat('txz_libs/' + f).st_mtime
        if tt > t: t = tt
        hash_list['txz_libs/'+f] = (int(tt), os.path.getsize(os.path.join('txz_libs', f)))
    if RunSubprocess(['jar', 'cvf', '../libs/txz_gen.jar', 'assets'], './dex_tmp') != 0:
        ExitWithError('zip assets files error!!!')
    os.system(r'rmdir /S /Q dex_tmp\assets')
    os.system(r'rmdir /S /Q dex_tmp\META-INF')
    if RunSubprocess(['jar', 'cvf', '../txz_libs/txz_gen.jar', '*'], './dex_tmp') != 0:
        ExitWithError('zip class files error!!!')
    try:
        os.makedirs('dex_tmp/assets/dexs')
    except:
        pass
    ANDROID_HOME = RunCommand('echo %ANDROID_HOME%').strip()
    if ANDROID_HOME == '%ANDROID_HOME%':
        ExitWithError('missing env configuration for ANDROID_HOME!!!')
    if not os.path.isdir(ANDROID_HOME):
        ExitWithError('wrong env configuration for ANDROID_HOME: %s!!!' % ANDROID_HOME)
    ver = '20.0.0'
    if not os.path.isdir(os.path.join(ANDROID_HOME, 'build-tools', ver)):
        for ver in os.listdir(ANDROID_HOME + '/build-tools'):
            if int(ver.split('.')[0]) >= 20: break
        print 'change dex build tool version to: ' + ver
    if os.system(r'cmd.exe /C "%s\build-tools\%s\dx.bat" --dex --output=dex_tmp\assets\dexs\txz_gen.dex txz_libs\txz_gen.jar' % ('%ANDROID_HOME%', ver)) != 0:
        ExitWithError('run jar2dex error, please check env[ANDROID_HOME]!!!')
    if RunSubprocess(['jar', 'uvf', '../libs/txz_gen.jar', 'assets'], './dex_tmp') != 0:
        ExitWithError('zip dex files error!!!')
    os.system(r'rmdir /S /Q dex_tmp')
    os.utime('libs/txz_gen.jar', (t, t))
    os.utime('txz_libs/txz_gen.jar', (t, t))
    hash_list['libs/txz_gen.jar'] = (int(t), os.path.getsize('libs/txz_gen.jar'))
    hash_list['txz_libs/txz_gen.jar'] = (int(t), os.path.getsize('txz_libs/txz_gen.jar'))
    fp = open('libs/check_jars.pyo', 'wb')
    fp.write(repr(hash_list))
    fp.close()
    os.utime('libs/check_jars.pyo', (t, t))
    
    
    
