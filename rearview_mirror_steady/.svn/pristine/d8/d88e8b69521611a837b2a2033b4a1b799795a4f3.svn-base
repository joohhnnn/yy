#coding=utf8
import os
import sys
import obtainJavaSrcPullPath

if len(sys.argv) != 3:
    print 'arg error'
    exit(0)
    
#print sys.argv

old_src = sys.argv[1]
src = sys.argv[2]

path_map = obtainJavaSrcPullPath.obtainPathMap(src)

for key in path_map.keys():
    #print '%s:%s'%(key, path_map.get(key))
    print key
    key_value = path_map.get(key)
    key_value = '..\\..\\src\\%s'%(key_value)
    key = '%s\\%s'%('old_src', key)
    print key_value
    obtainJavaSrcPullPath.copy(key_value, key, False)
