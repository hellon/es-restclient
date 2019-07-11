package com.liuvlun.es.storage.base;

import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.google.common.base.Joiner;
import com.google.common.io.Files;


import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Created by liuhailo on 2019/6/22.
 */
public class DefaultIndexMapping implements IndexMapping<String> {
    Log log = LogFactory.get();
    public DefaultIndexMapping(){
    }

    public String getMapping(String indexName) {
        ClassPathResource classPathResource = new ClassPathResource(MAPPINGS_PATH + indexName + MAPPINGS_SUFFIX);
        try {
            return Joiner.on(System.getProperty("line.separator"))
                    .join(Files.readLines(classPathResource.getFile(), Charset.defaultCharset()));
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }
        return null;
    }
}
