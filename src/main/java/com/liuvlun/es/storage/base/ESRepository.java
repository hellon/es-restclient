package com.liuvlun.es.storage.base;


import com.liuvlun.es.storage.core.EntityMapper;

/**
 * Created by liuhailo on 2019/6/18.
 */
public interface ESRepository<T> {
    EntityMapper<T> getEntityMapper();
}
