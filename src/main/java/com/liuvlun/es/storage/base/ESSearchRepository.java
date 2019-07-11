package com.liuvlun.es.storage.base;

import org.elasticsearch.index.query.QueryBuilder;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by liuhailo on 2019/6/18.
 */
public interface ESSearchRepository<T,ID extends Serializable> extends ESCrudRepository<T,ID>{


    Iterable<T> search(QueryBuilder var1) throws IOException;

    void refresh() throws IOException;

    void refresh(String... indices) throws IOException;

    Class<T> getEntityClass();


    boolean createIndex(Class<T> clazz) throws IOException;
}
