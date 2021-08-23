#code=utf-8
import os
extLibs = ".\extends\libs"
subDirList = os.listdir(extLibs)

def build_arch(project, arch):
    cmdline = "release_anyone %s %s" % (project, arch)
    os.system(cmdline)

def build_project(project):
    strProject = extLibs + "\\" + project
    archs = os.listdir(strProject)
    for arch in archs:
        if (os.path.isdir(strProject + "\\" + arch)):
            build_arch(project, arch)
for project in subDirList:
    build_project(project)
