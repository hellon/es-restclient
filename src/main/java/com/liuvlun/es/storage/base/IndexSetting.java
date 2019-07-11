package com.liuvlun.es.storage.base;


/**
 * Created by liuhailo on 2019/6/18.
 */
public interface IndexSetting<T> {
    String SETTINGS_PATH = "/settings/";
    String MAPPINGS_SUFFIX = ".json";
    T getSetting(String indexName);
}
