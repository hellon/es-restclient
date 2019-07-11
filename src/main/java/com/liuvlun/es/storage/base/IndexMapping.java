package com.liuvlun.es.storage.base;

/**
 * Created by liuhailo on 2019/6/18.
 */

public interface IndexMapping<T> {
    String MAPPINGS_PATH = "/mappings/";
    String MAPPINGS_SUFFIX = ".json";
    T getMapping(String indexName);
}
