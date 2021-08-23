import os
import sys

TXZ_TOOLS_DIR = '../../tools'

JAVA_VERSION_FILE = 'src/com/txznet/comm/version/TXZVersion.java'

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


def GetSvnBranchVersion(d='.'):
    svninfo = RunCommand(('%s info "%s"' % (os.path.join(TXZ_TOOLS_DIR, r'sliksvn\svn.exe'), d)).replace('/', "\\"))
    for s in svninfo.splitlines():
        s = s.strip()
        if s.startswith('Relative URL:'):
            svn_ver = "REL"
            if s.find('/rearview_mirror/') > 0:
                svn_ver = "DEV"
            elif s.find('/rearview_mirror_new_features/') > 0:
                svn_ver = "NEW"
            print '[%s] svn brach version [%s]' % (d, svn_ver)
            return svn_ver
    sys.stderr.write("get [%s] svn info error\n" % d)
    return "UNKNOW"

