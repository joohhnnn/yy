#coding=utf8
import os

def copy(source, target, bDir):
    if bDir == True:
        cp_cmd = 'xcopy /S /Y %s %s'%(source, target)
    else:
        cp_cmd = 'copy /B /Y %s %s'%(source, target)
    os.system(cp_cmd)


def readSourePath(src):
    #print src
    fullPath = None
    f = open(src)
    lines = f.readlines(10)
    for line in lines:
        if line.startswith('package '):
            #print line
            if line.endswith('\n'):
                line = line.replace('\n', '')
            #print line
            tail = line.find(';')
            fullPath = line[len('package '):tail]
            break
    if f != None:
        f.close()
        
    return fullPath
        

src_dir = 'src'

def obtainPathMap(src):
    path_map = {}
    src_list = os.listdir(src_dir)
    #print src_list

    for src in src_list:
        full_dir = readSourePath("%s\%s"%(src_dir, src))
        if full_dir == None:
            print 'error : %s'%(src)
            continue
        full_dir = full_dir.replace('.', '\\')
        full_path = '%s\\%s'%(full_dir, src)
        path_map[src] = full_path

    return path_map

#print obtainPathMap(src_dir)
        
