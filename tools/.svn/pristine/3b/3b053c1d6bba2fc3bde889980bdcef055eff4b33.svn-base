# -*- coding: utf-8 -*-
import os,sys, re

__re_find = re.compile(r'''(?P<REM1>\/\/)|(?P<REM2>\/\*)|(?P<STR>\")|(?P<CHR>\')|(?P<CLASS>(?:(?:class|interface|enum)\s+?|new\s+?)[^;]+?\{)|(?P<LOGIC>\{)|(?P<END>\})|(?P<LINE>\n)|(?P<SIGN>;)''', re.M);
__re_rem1_end = re.compile(r'(?P<LINE>\n)', re.M);
__re_rem2_end = re.compile(r'(?P<REM3>\*\/)', re.M);
__re_str_end = re.compile(r'(?P<STR>[^\\]")', re.M);
__re_chr_end = re.compile(r"(?P<CHR>[^\\]')", re.M);


def checkNotExprStart(s, loc):
    left = s[:loc].strip()
    right = s[loc:].strip()
    left_row = left[(left.rfind('\n')):].strip()
    if left.endswith('else'):return False
    if left.endswith('('):return False
    if left.endswith(')'):return False
    if left.endswith(','):return False
    if right.startswith('super('):return False
    if right.startswith('this('):return False
    if right.startswith('while'):return False
    if right.startswith(','):return False
    if right.startswith(')'):return False
    if right.startswith('.'):return False
    if right.startswith(';'):return False
    if right.startswith(':'):return False
    if right.startswith('?'):return False
    if right.startswith('+'):return False
    if right.startswith('-'):return False
    if right.startswith('*'):return False
    if right.startswith('/'):return False
    if right.startswith('%'):return False
    if right.startswith('>'):return False
    if right.startswith('<'):return False
    if right.startswith('='):return False
    if right.startswith('&'):return False
    if right.startswith('|'):return False
    if left_row.startswith('for'):return False
    return True


def findRetEnd(s, start, end):
    i = start
    cls_n = -1
    func_n = []
    while i < len(s):
        m = __re_find.search(s, i, end)
        #print m.group(0)
        if m.group('CLASS') is not None: #类起始符
            cls_n += 1
            func_n.append(0)
        elif m.group('LOGIC') is not None: #逻辑起始符
            func_n[cls_n] += 1
        elif m.group('REM1') is not None: #单行注释
            m = __re_rem1_end.search(s, m.end(0)) #查找单行注释结束符
        elif m.group('REM2') is not None: #多行注释
            m = __re_rem2_end.search(s, m.end(0)) #查找多行注释结束符
        elif m.group('STR') is not None: #字符串
            m = __re_str_end.search(s, m.start(0)) #查找字符串结束符
        elif m.group('CHR') is not None: #字符串
            m = __re_chr_end.search(s, m.start(0)) #查找字符结束符
        elif m.group('RET') is not None: #返回语句处理，返回语句后不能插入代码
            i = findRetEnd(s, m.end(0), end) #查找下一轮
            continue
        elif m.group('END') is not None: #结束符
            if func_n[cls_n] > 0: #配对的是非类起始符
                func_n[cls_n] -= 1
            else: #配对的是类起始符
                func_n = func_n[:-1]
                cls_n -= 1
        elif m.group('SIGN'):
            if cls_n == -1:
                return m.end(0)
        else:
            pass
        i = m.end(0)


def procContent(s, start, end, name, num, tab=0):
    i = start
    cls_n = -1
    func_n = []
    line = num
    r = ''
    endExpr = False
    while i < end:
        proc = False
        m = __re_find.search(s, i, end)
        if m is None:
            r += s[i:end]
            break
        #print m.group(0)
        if m.group('CLASS') is not None: #类起始符
            endExpr = False
            cls_n += 1
            func_n.append(0)
            print '%sbegin class at line: %d' % ((' ' * (cls_n+tab+1)), line)
        elif m.group('LOGIC') is not None: #逻辑起始符
            endExpr = True
            func_n[cls_n] += 1
            print '%sbegin logic at line: %d' % ((' ' * (cls_n+tab+2)), line)
            #考虑到构造函数第一行不能插入代码，暂时不处理函数入口
            #r += s[i:m.end(0)] + ('android.util.Log("pbh_trace", "pbh_trace logic[%s:%d]");' % (name, line))
            #proc = True
        elif m.group('REM1') is not None: #单行注释
            endExpr = False
            m = __re_rem1_end.search(s, m.end(0)) #查找单行注释结束符
            if m is None: #最后的单行注释
                r += s[i:] 
                break
            line += 1
        elif m.group('REM2') is not None: #多行注释
            endExpr = False
            mm = __re_rem2_end.search(s, m.end(0)) #查找多行注释结束符
            if mm is None: #出错
                print '!!!!!!!!!!!!! no match mutil-line rem end'
                return r
            #计算行号
            line += s[m.start(0):mm.end(0)].count('\n')
            m = mm
        elif m.group('STR') is not None: #字符串
            endExpr = False
            m = __re_str_end.search(s, m.start(0)) #查找字符串结束符
            if m is None: #出错
                print '!!!!!!!!!!!!! no match string end'
                return r
        elif m.group('CHR') is not None: #字符串
            endExpr = False
            m = __re_chr_end.search(s, m.start(0)) #查找字符串结束符
            if m is None: #出错
                print '!!!!!!!!!!!!! no match char end'
                return r
        elif m.group('LINE') is not None: #新行
            line += 1
        elif m.group('END') is not None: #结束符
            #print cls_n
            if func_n[cls_n] > 0: #配对的是非类起始符
                print '%send logic at line: %d' % ((' ' * (cls_n+tab+2)), line)
                func_n[cls_n] -= 1
            else: #配对的是类起始符
                print '%send class at line: %d' % ((' ' * (cls_n+tab+1)), line)
                func_n = func_n[:-1]
                cls_n -= 1
        elif m.group('SIGN') is not None: #分号
            if cls_n >= 0 and func_n[cls_n] >= 1: #处于函数中
                if endExpr and checkNotExprStart(s, i): #结束了表达式并且不在构造函数首行
                    print '%sadd trace log at line: %d' % ((' ' * (cls_n+tab+3)), line)
                    r += ('android.util.Log.d("pbh_trace", "pbh_trace line[%s:%d]");' % (name, line)) + s[i:m.end(0)]
                    proc = True
            endExpr = True
            pass
        else:
            pass
        if proc == False:
            r += s[i:m.end(0)]
        i = m.end(0)
    if cls_n != -1:
        print '!!!!!!!!!!!!! no match class end: %d' % cls_n
    return r, line-num
                

def procFile(f, add):
    #print str(add) + ' ' + f
    if add == '-del':
        if not f.endswith('.java.bak'): return
        print 'begin recover: ' + f
        os.unlink(f[:-4])
        os.rename(f, f[:-4])
        print 'end recover: ' + f
        return
    if not f.endswith('.java'): return
    print 'begin proc: ' + f
    fp = open(f)
    d = fp.read()
    fp.close()
    fp = open(f + '.tmp', 'w')
    name = f.replace('\\', '.')
    r, line = procContent(d, 0, len(d), name, 1, 0)
    fp.write(r)
    fp.close()
    if add == '-bak':
        os.rename(f, f + '.bak')
        os.rename(f + '.tmp', f)
    elif add == '-tmp':
        pass
    else:
        os.unlink(f)
        os.rename(f + '.tmp', f)
    print 'end proc: ' + f

def procPath(d, add):
    if os.path.isfile(d):return procFile(d, add)
    if not os.path.isdir(d):return
    for f in os.listdir(d):
        if f == '.' or f == '..':continue
        f = os.path.join(d, f)
        procPath(f, add)

if __name__ == '__main__':
    if len(sys.argv) == 1:
        procPath("test.java", '-tmp')
    else:
        for arg in sys.argv[2:]:
            procPath(arg, sys.argv[1])
    


        
            
