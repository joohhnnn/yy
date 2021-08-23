# -*- coding: gb2312 -*-
import sys
import os
import subprocess
import zipfile
import shutil

svn = os.path.join(os.path.dirname(sys.argv[0]), r'..\tools\sliksvn\svn.exe')
#svn = 'svn'


def LogE(msg):
    sys.stderr.write('%s\n' % msg)

def LogD(msg):
    sys.stdout.write('%s\n' % msg)

def RunCmd(cmd):
    p = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    text = p.stdout.read()
    sts = p.returncode
    if sts is None: sts = 0
    if text[-1:] == '\n': text = text[:-1]
    return sts, text

def GetSVNStatus(ref, root):
    (r, lst) = RunCmd([svn, 'status', root])
    add_files = []
    del_files = []
    mod_files = []
    if r != 0:
        return (r, add_files, del_files, mod_files)
    for fc in lst.splitlines():
        fc = fc.strip()
        if fc == '':continue
        fc = fc.split(' ')
        if len(fc) < 2: continue
        c = fc[0].strip()
        f = fc[-1].strip().replace('\\', '/')
        if f.startswith(ref + '/'): f = f[(len(ref)+1):]
        if c == 'M':
            mod_files.append(f)
        elif c == '?':
            add_files.append(f)
        elif c == '!':
            del_files.append(f)
    return (r, add_files, del_files, mod_files)

def GetSVNVer(root):
    (r, ver) = RunCmd([svn, 'info', '--show-item', 'revision', root])
    if r != 0: return -1
    try:
        return int(ver.strip())
    except:
        LogE(ver)
        return -1
        
def GetSVNUrl(root):
    (r, url) = RunCmd([svn, 'info', '--show-item', 'relative-url', root])
    if r != 0:
        LogE('get SVN[%s] url error %d: %s' % (root, r, lst))
        return ''
    return url.strip()

def ProcSVNVer(vers, ref, root, root_ver, skip_files):
    LogD('begin proc directory[%s]' % root)
    for f in os.listdir(root):
        if f == '.' or f == '..' or f == '.svn': continue
        ffull = os.path.join(root, f).replace('\\', '/')
        fref = ffull[(len(ref)+1):]
        if fref in skip_files: continue
        ver = GetSVNVer(ffull)
        if ver == -1:
            LogD('no SVN version [%s]' % (ffull))
            continue
        if ver != root_ver:
            vers.append((fref, ver))
            LogD('get SVN version [%s]: %d' % (ffull, ver))
        else:
            pass
            #LogD('same SVN version [%s] with [%s]' % (ffull, root))
        if os.path.isdir(ffull):
            r = ProcSVNVer(vers, ref, ffull, ver, skip_files)
            if r != 0: return r
    return 0

def FormatRoot(root):
    root = os.path.abspath(root).replace('\\', '/')
    while True:
        if root.endswith('/'):
            root = root[:-1]
        elif root.endswith('/.'):
            root = root[:-2]
        else:
            break
    return root

def SaveSVNVer(root, doc, names):
    root = FormatRoot(root)
    if len(names) == 0: names = ['.']
    vers = []
    add_files = []
    del_files = []
    mod_files = []
    zf = zipfile.ZipFile(doc, 'w')
    urls = {}
    for name in names:
        name_root = FormatRoot(os.path.join(root, name))
        url = GetSVNUrl(name_root)
        if url == '':
            LogE('get SVN[%s] url failed' % name_root)
            return -2
        urls[name] = url
        (r, adds, dels, mods) = GetSVNStatus(root, name_root)
        if r != 0:
            LogE('get SVN[%s] status failed' % name_root)
            return -3
        name_ver = GetSVNVer(name_root)
        if name_ver < 0:
            LogE('get SVN[%s] version failed' % name_root)
            return -4
        vers.append((name, name_ver))
        r = ProcSVNVer(vers, root, name_root, name_ver, (dels+adds+mods))
        if r != 0: return r
        add_files += adds
        del_files += dels
        mod_files += mods
    info = repr({
            'url' : urls,
            'ver' : vers,
            'add' : add_files,
            'del' : del_files,
            'mod' : mod_files,
            })
    LogD(info)
    zf.writestr('.svn', info, compress_type=zipfile.ZIP_DEFLATED)
    for f in (add_files+mod_files):
        LogD('begin zip file[%s]' % f)
        zf.write(os.path.join(root, f), f, compress_type=zipfile.ZIP_DEFLATED)
    zf.close()
    return 0

def LoadSVNVer(doc, root):
    root = FormatRoot(root)
    zf = zipfile.ZipFile(doc, 'r')
    zf_info = zf.getinfo('.svn')
    info = eval(zf.open(zf_info).read())
    for name, name_url in info['url'].items():
        name_root = FormatRoot(os.path.join(root, name))
        url = GetSVNUrl(name_root)
        if url != name_url:
            LogE('get SVN[%s] url[%s] not match[%s]' % (name_root, url, name_url))
            return -5
    for f, v in info['ver']:
        ver_root = FormatRoot(os.path.join(root, f))
        LogD('begin update [%s] to revision[%d]' % (ver_root, v))
        cmd = [svn, 'update', '--revision', '%s' % v, ver_root]
        (r, msg) = RunCmd(cmd)
        if r != 0:
            LogE('update SVN[%s] to revision[%d] failed' % (ver_root, v))
            return -6
    for f in info['del']:
        f = FormatRoot(os.path.join(root, f))
        if os.path.exists(f):
            LogD('begin remove [%s]' % f)
            if os.path.isdir(f):
                shutil.rmtree(f)
            elif os.path.isfile(f):
                os.remove(f)
    for f in (info['add']+info['mod']):
        LogD('begin unzip [%s]' % f)
        zf_info = zf.getinfo(f)
        data = zf.open(zf_info).read()
        fp = open(os.path.join(root, f), 'wb')
        fp.write(data)
        fp.close()
    zf.close()
    LogD('load complete')
    return 0
    
def Main():
    if len(sys.argv) >= 3:
        if os.path.isfile(sys.argv[1]):
            return LoadSVNVer(sys.argv[1], sys.argv[2])
        elif os.path.isdir(sys.argv[1]):
            return SaveSVNVer(sys.argv[1], sys.argv[2], sys.argv[3:])
    LogE('''================================================================
  打包：
    SVN_Ver_Pack.py 归档目录 归档文件 [子目录列表]
  还原：
    SVN_Ver_Pack.py 归档文件 还原目录
================================================================
''')
    return -1

if __name__ == '__main__':
    os._exit(Main())


