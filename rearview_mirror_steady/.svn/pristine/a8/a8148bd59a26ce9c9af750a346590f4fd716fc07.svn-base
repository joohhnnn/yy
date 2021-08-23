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

def list_project_for_select(projectList):
    index = 0
    for project in projectList:
        print "[%d]%s"%(index, project)
        index = index + 1
                        
while True:        
    list_project_for_select(subDirList)
    inputParam  = raw_input("please touch number[0-%d] for buiding target project or 'q' exit build: "%(len(subDirList) - 1))
    if inputParam == 'q':
        print "exit"
        break
    try:
        index = int(inputParam)
    except:
        continue
    
    if index >= 0 and index < len(subDirList):
        print subDirList[index]
        build_project(subDirList[index])
        break
