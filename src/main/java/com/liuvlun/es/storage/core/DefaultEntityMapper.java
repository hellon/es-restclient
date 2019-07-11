package com.liuvlun.es.storage.core;


import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Created by liuhailo on 2019/6/18.
 */
public class DefaultEntityMapper<T> implements EntityMapper<T>{
    Log log = LogFactory.get();
    private ObjectMapper objectMapper;

    public DefaultEntityMapper() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        //objectMapper.registerModule(new CustomGeoModule());
    }

    public  String mapToString(T object) {
        try {
            if(object != null){
                return objectMapper.writeValueAsString(object);
            }
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(),e);
        }
        return null;
    }

    public   T mapToObject(String source, Class<T> clazz){
        try {
            if(source != null){
                return objectMapper.readValue(source, clazz);
            }
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }
        return null;
    }
}
