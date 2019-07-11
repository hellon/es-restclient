package com.liuvlun.es.storage.base;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/**
 * Created by liuhailo on 2019/6/18.
 */
@Data
public abstract class ElasticsearchDocument<ID extends Serializable> {

    public static final String DEFAULT_DOC_TYPE_NAME = "_doc";

    private ID id;

    private Long version;

    private Date createdAt;
    private Date lastChange;

}
