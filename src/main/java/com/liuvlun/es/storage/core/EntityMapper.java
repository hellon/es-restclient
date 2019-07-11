package com.liuvlun.es.storage.core;

import java.io.IOException;

/**
 * Created by liuhailo on 2019/6/18.
 */
public interface EntityMapper<T> {
    public  String mapToString(T var);

    public T mapToObject(String source, Class<T> clazz);
}
