import os
import re

prj_name = raw_input("Please input the project name: ")

print 'generate files for ' + prj_name

pkg_name = prj_name.lower()

def replaceFileContent(fpath):
    fp = open(fpath, 'rb')
    s = fp.read()
    fp.close()
    s = s.replace('TXZDGNav', prj_name)
    s = s.replace('txzdgnav', pkg_name)
    fp = open(fpath, 'wb')
    fp.write(s)
    fp.close()

os.rename('src/com/txznet/txzdgnav', 'src/com/txznet/' + pkg_name)

def procDirs(d):
    for n in os.listdir(d):
        n = os.path.join(d, n)
        if os.path.isdir(n):procDirs(n)
        if not os.path.isfile(n):continue
        replaceFileContent(n)

procDirs('.')

os.system('pause')
