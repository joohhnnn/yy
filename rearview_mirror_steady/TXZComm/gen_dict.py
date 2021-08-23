import random

seed="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ01234567890"
seed=random.sample(seed,len(seed))

def genDict(n, lst=['T']):
    ret = [] + lst
    for t in lst:
        for s in seed:
            ret.append(t+s)
            if len(ret) > n:return ret
    return genDict(n, ret)

if __name__ == '__main__':
    lst = genDict(9999)
    fp = open('../TXZComm/dictionary.txt', 'w')
    for t in lst:
        #print t
        fp.write(t + '\n')
    fp.close()
    print '!!!!!!! done'







