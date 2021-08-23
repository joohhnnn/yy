from txz_compile_tool import *

import datetime

def GenPbSrc(d):
    RunCommandAtGeneral('mkdir pbsrc')
    d = d.replace('/', "\\")
    for f in os.listdir(os.path.join(TXZ_GENERAL_DIR, d)):
        if f.endswith('.proto'):
            f = os.path.join(d, f)
            cmd = r'jni\tools\compile\protoc --javanano_out=optional_field_style=reftypes:./pbsrc -I%s %s' % (d, f)
            print cmd
            RunCommandAtGeneral(cmd)

def GenVersionFile():
    svn_ver = GetSvnVersion()
    t = datetime.datetime.now().strftime("%Y%m%d%H%M%S")
    author = os.getenv('COMPUTERNAME')
    d = '''
package com.txznet.txz.module.version;

public class TXZVersion { 
\tpublic static final int SVNVERSION =%s; 
\tpublic static final String COMPUTER = "%s"; 
\tpublic static final String PACKTIME= "%s"; 
}

''' % (svn_ver, author, t)
    try:
        os.makedirs(os.path.dirname(JAVA_VERSION_FILE))
    except:
        pass
    f = open(JAVA_VERSION_FILE, 'w')
    f.write(d)
    f.close()
    


def GenAssertHash(d):
    if os.path.isdir(d):
        open(os.path.join(d, "check.hash"), 'wb').write(DirHash(d))
    

def Main():
    #GenPbSrc('jni/protos/ui')
    GenVersionFile()
    GenDexFiles()
    

if __name__ == '__main__':
    Main()
