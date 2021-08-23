#coding=utf8
import os
import re

def copy(source, target, bDir):
    if bDir == True:
        cp_cmd = 'xcopy /E /Y %s %s'%(source, target)
    else:
        cp_cmd = 'copy /B /Y %s %s'%(source, target)
    os.system(cp_cmd)
    print cp_cmd

cwd = os.getcwd()
print cwd

sourceProject = "..\\TXZCore"
children = os.listdir(sourceProject)
#print children

ignores = "^bin$|^build$|^gen$|^\..*"
for child in children:
    source = "%s\\%s"%(sourceProject, child)
    target = ".\\%s"%(child)
    if re.match(ignores, child):
        print 'ignore %s'%(child)
        continue
    
    if os.path.isdir(source):
        if not os.path.exists(target):
            print "create %s"%(target)
            os.mkdir(target)
        copy(source, target, True)
    else:
       copy(source, target, False)
