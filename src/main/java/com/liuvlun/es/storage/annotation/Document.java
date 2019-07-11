package com.liuvlun.es.storage.annotation;

import com.liuvlun.es.storage.base.DefaultIndexMapping;
import com.liuvlun.es.storage.base.DefaultIndexSetting;
import com.liuvlun.es.storage.base.IndexMapping;
import com.liuvlun.es.storage.base.IndexSetting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Document {

    public String indexName();

    UniqueConstraint[] uniqueConstraints() default {};

    Class<? extends IndexMapping> mappingGenerator() default DefaultIndexMapping.class;
    Class<? extends IndexSetting> settingGenerator() default DefaultIndexSetting.class;
}
