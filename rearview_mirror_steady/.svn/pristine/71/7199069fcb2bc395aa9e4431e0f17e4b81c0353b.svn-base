#coding=utf8
import os
import re

def rm(target, bDir):
    if bDir == True:
        cp_cmd = 'del /S /Q %s'%(target)
        os.system(cp_cmd)
        cp_cmd = 'rd /S /Q %s'%(target)
        os.system(cp_cmd)
    else:
        cp_cmd = 'rd /S /Q %s'%(target)
    print cp_cmd

children = ['src', 'txz_libs', 'libs', 'assets', 'res', 'bin', 'tools', 'document', 'gen']
for child in children:
    if os.path.exists(child):
        if os.path.isdir(child):
            rm(child, True)
    else:
        pass
