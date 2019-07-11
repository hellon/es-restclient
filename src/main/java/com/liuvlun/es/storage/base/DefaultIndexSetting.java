package com.liuvlun.es.storage.base;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.google.common.base.Joiner;
import com.google.common.io.Files;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;


/**
 * Created by liuhailo on 2019/6/19.
 */
public class DefaultIndexSetting implements IndexSetting<String> {

    Log log = LogFactory.get();
    public String getSetting(String indexName) {
        String settingJson = "{\"number_of_shards\":\"1\",\"number_of_replicas\":1}";
        URL url = this.getClass().getClassLoader().getResource(SETTINGS_PATH + indexName + MAPPINGS_SUFFIX);

        if(url != null){
            try {
                return Joiner.on(System.getProperty("line.separator"))
                        .join(Files.readLines(FileUtil.file(url), Charset.defaultCharset()));
            } catch (IOException e) {
                log.error(e.getMessage(),e);
            }
        }

        return settingJson;
    }
}
