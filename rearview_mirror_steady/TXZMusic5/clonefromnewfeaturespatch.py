# coding=utf8
import os
import re
import shutil

os.system("svn up")

cpath = str(os.getcwd())
cdir = os.path.basename(cpath)

source = "..\\..\\rearview_mirror_new_features\\" + cdir + "\src\main"

skipList = open(".\\scriptKeepFile.txt").readlines()
reg = ''
for i in range(len(skipList)):
    reg += skipList[i].replace("\n", '') + '$'
    if not i == (len(skipList) - 1):
        reg += '|'

for folderName, subfolders, filenames in os.walk(source):
    for filename in filenames:
        ori = os.path.join(folderName, filename)
        if len(reg) > 0 and re.match(reg, filename):
            print("ignore file:" + ori)
            continue
        if not os.path.exists(folderName.replace(
            "rearview_mirror_new_features", "rearview_mirror_steady")):
            os.makedirs(folderName.replace(
            "rearview_mirror_new_features", "rearview_mirror_steady"))
        dst = os.path.join(folderName.replace(
            "rearview_mirror_new_features", "rearview_mirror_steady"), filename)
        print('copy, from:' + ori + ', to:' + dst)
        shutil.copy(ori, dst)

print('finish')
