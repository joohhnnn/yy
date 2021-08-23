# -*- coding: utf-8 -*-
import re
import time
import sys

def printResult(fp, s):
    print s
    fp.write(s)
    fp.write('\n')

def calTime(s):
    t = '2016-' + s[:s.rfind('.')]
    return time.mktime(time.strptime(t,'%Y-%m-%d %H:%M:%S'))


def procFile(f, pids):
    fp = open(f)
    r = fp.readlines()
    fp.close()
    fp = open(f + '.getThreadLog.log', 'w')
    ret = {}
    keyis = {} #统计进程下出现的日志的最后时间
    keyi_count1 = {} #出现次数计数
    keyi_count2 = {} #最后3秒计数
    for i in range(len(r)):
        for pid in pids:
            m = re.search(r'^(.+?)\s+?%s\s+?(\d+?)\s+?\w' % pid, r[i])
            if m is not None:
                tids = ret.get(pid, {})
                tids[m.group(2)] = (i, m.group(1), r[i])
                tids[''] = m.group(1)
                ret[pid] = tids
    #print ret
    for pid in ret:
        rrr = ret[pid]
        t = rrr['']
        printResult(fp, '=============%s===========最后日志时间: %s' % (pid, t))
        for tid in rrr:
            if tid == '': continue
            i, t, s = rrr[tid]
            s = s.strip()
            #print 'tid=%09d line=%09d:  %s' % (int(tid), i, s)
            printResult(fp, '%09d:  %s' % (i, s))
            nt = calTime(t)
            if s[-1] == ']':
                idx = s[s.rfind('['):]
            else:
                idx = s[s.find(':')+1:].strip()
                idx = ' '.join(re.findall(r'(?:^|\W)([_a-zA-Z]\w+)', idx))
            ks = keyis.get(pid, {})
            nnt = ks.get(idx, nt)
            if nnt < nt:
                ks[idx] = nt
            else:
                ks[idx] = nnt
            keyis[pid] = ks
        printResult(fp, '=====================================================')
    for pid in keyis:
        ks = keyis[pid]
        t = ret[pid]['']
        last_time = calTime(t)
        for k in ks:
            keyi_count1[k] = keyi_count1.get(k, 0)+1
            if (ks[k] < last_time - 3):
                keyi_count2[k] = keyi_count2.get(k, 0)
            else:
                keyi_count2[k] = keyi_count2.get(k, 0)+1
    keyi1 = [] #每个进程最后3秒都出现
    keyi2 = [] #每个进程都出现
    keyi3 = [] #有2个以上进程最后3秒出现过
    keyi4 = [] #最后3秒出现过
    for k in keyi_count1:
        n = keyi_count1[k]
        n3 = keyi_count2[k]
        if n == len(pids) and n3 == n:
            keyi1.append((n, n3, k))
        elif n == len(pids):
            keyi2.append((n, n3, k))
        elif n >= 2 and n3 >= n:
            keyi3.append((n, n3, k))
        elif n3 >= 1:
            keyi4.append((n, n3, k))
    printResult(fp, '================极度可疑：每个进程最后3秒都出现================')
    for n,n3,k in keyi1: printResult(fp, '%02d/%02d： %s' % (n,n3,k))
    printResult(fp, '================深度可疑：每个进程都出现================')
    for n,n3,k in keyi2: printResult(fp, '%02d/%02d： %s' % (n,n3,k))
    printResult(fp, '================严重可疑：有2个以上进程最后3秒出现过================')
    for n,n3,k in keyi3: printResult(fp, '%02d/%02d： %s' % (n,n3,k))
    printResult(fp, '================一般可疑：最后3秒出现过================')
    for n,n3,k in keyi4: printResult(fp, '%02d/%02d： %s' % (n,n3,k))
    
    fp.close()
    

def preFile(fName):
    fp1 = open(fName)
    r = fp1.readlines()
    fp1.close()
    plist = []
    n = 0
    for i in range(len(r)):
        ri = r[i]
        begin = ri.find("Process com.txznet.txz (pid")
        if begin != -1:
            n = n + 1
            if n > 0:
                ed = ri.find(")")
                be = begin+28
                p = ri[be:ed];
                plist.append(ri[be:ed].strip())
    return plist
    
if __name__ == '__main__':
    for logFile in sys.argv[1:]:
        print 'begin process file: ' + logFile
        pidlist=preFile(logFile)
        procFile(logFile, pidlist)
