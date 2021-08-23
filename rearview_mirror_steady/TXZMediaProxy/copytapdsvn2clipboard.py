import re
import os
import pyperclip


cpath = str(os.getcwd())
cdir = os.path.basename(cpath)

def getsvnrevision():
    os.system("svn up")
    return os.popen("svn info .").readlines()


svninfo = str(getsvnrevision())

print("===============")

matchObj = re.search(r"Last Changed Rev: (\d+)", svninfo, re.MULTILINE)

if matchObj:
    svnversion = matchObj.group(1)
    print(svnversion)
    pyperclip.copy("rearview_mirrors_steady_" + cdir + "_" + svnversion)
else:
    print("not found")
