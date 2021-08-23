from txz_compile_tool import *

import datetime


def GenVersionFile():
    svn_ver = GetSvnVersion()
    branch_ver = GetSvnBranchVersion()
    t = datetime.datetime.now().strftime("%Y%m%d%H%M%S")
    author = os.getenv('COMPUTERNAME')
    d = '''
package com.txznet.comm.version;

public class TXZVersion { 
\tpublic static final int SVNVERSION =%s; 
\tpublic static final String COMPUTER = "%s"; 
\tpublic static final String PACKTIME= "%s";
\tpublic static final String BRANCH= "%s"; 
}

''' % (svn_ver, author, t, branch_ver)
    try:
        os.makedirs(os.path.dirname(JAVA_VERSION_FILE))
    except:
        pass
    f = open(JAVA_VERSION_FILE, 'w')
    f.write(d)
    f.close()


def Main():
    GenVersionFile()
    

if __name__ == '__main__':
    Main()
