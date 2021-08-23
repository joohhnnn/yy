package com.txznet.rxflux;


import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * RxAction对象池
 *
 * @author zackzhou
 * @date 2018/12/20,14:07
 */

class RxActionPool {

    private static ObjectPool<RxAction> objectPool = new GenericObjectPool<>();

    private RxActionPool() {

    }

    public static ObjectPool<RxAction> getPool() {
        return objectPool;
    }
}
