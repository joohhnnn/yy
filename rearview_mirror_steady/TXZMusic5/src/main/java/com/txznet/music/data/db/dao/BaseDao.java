package com.txznet.music.data.db.dao;

import java.util.List;

interface BaseDao<T, K> {

    List<T> listAll();

    long saveOrUpdate(T t);

    T get(K id);

    int delete(T t);

    int deleteAll();
}
