package com.liuvlun.es.storage.base;


import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import com.liuvlun.es.storage.annotation.UniqueConstraint;
import com.liuvlun.es.storage.core.DefaultEntityMapper;
import com.liuvlun.es.storage.core.IdGenerator;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.CountRequest;
import org.elasticsearch.client.core.CountResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

import com.liuvlun.es.storage.annotation.Document;
import com.liuvlun.es.storage.core.EntityMapper;
import org.elasticsearch.search.sort.SortOrder;

/**
 * Created by liuhailo on 2019/6/18.
 */
public abstract class AbstractESRepository<T extends ElasticsearchDocument, ID extends Serializable> implements ESSearchRepository<T, ID> {

    private final RestHighLevelClient highLevelClient;

    private Map<String,String> sortMap = new HashMap<>();

    private EntityMapper<T> mapper = new DefaultEntityMapper<>();

    public AbstractESRepository(final RestHighLevelClient highLevelClient) {
        this.highLevelClient = highLevelClient;
    }

    /**
     * Internal document id generator.
     */
    protected static final IdGenerator ID_GENERATOR = new IdGenerator();


    public T index(final T var1) throws IOException {
        final Document document = var1.getClass().getAnnotation(Document.class);
        final UniqueConstraint[] constraints = document.uniqueConstraints();
        for (final UniqueConstraint constraint : constraints) {
            final String[] columnNames = constraint.columnNames();
            if (columnNames.length == 0) {
                continue;
            }
            final BoolQueryBuilder boolQuery = new BoolQueryBuilder();
            for (final String columnName : columnNames) {
                try {
                    final Field field = var1.getClass().getDeclaredField(columnName);
                    field.setAccessible(true);
                    final Object value = field.get(var1);
                    boolQuery.must(QueryBuilders.matchQuery(columnName, value));
                } catch (final IllegalAccessException | NoSuchFieldException e) {
                    throw new IOException();
                }
            }
            if (var1.getId() == null && existsDocument(boolQuery)) {
                throw new ConstraintValidationException(
                        String.format("[%s, %s]: Unique constraint violation.",
                                var1.getClass().getSimpleName(), "INDEX")
                );
            }
        }
        generateId(var1);
        final IndexRequest request = new IndexRequest(document.indexName());
        var1.setCreatedAt(new Date());
        request.id(var1.getId().toString());
        request.source(getEntityMapper().mapToString(var1), XContentType.JSON);
        settingIndexRequest(var1, request);
        final IndexResponse indexResponse = highLevelClient.index(request, RequestOptions.DEFAULT);
        var1.setVersion(indexResponse.getVersion());
        return var1;
    }


    protected void generateId(final T var1) {
        if (var1.getId() == null) {
            var1.setId(String.valueOf(ID_GENERATOR.nextId()));
        }
    }

    protected <S extends T> void settingIndexRequest(final S var1, final IndexRequest request) {
    }

    /**
     *
     * @param var1 QueryBuilder objects can  be created using the QueryBuilders utility class
     * @return List
     * @throws IOException
     */
    public Iterable<T> search(final QueryBuilder var1) throws IOException {
        final SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(indexName());
        final SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        sourceBuilder.sort("createdAt", SortOrder.DESC);

        for(Map.Entry<String,String> entry:this.sortMap.entrySet()){
            if("ASC".equals(entry.getValue().toUpperCase())){
                sourceBuilder.sort(entry.getKey(), SortOrder.ASC);
            }else {
                sourceBuilder.sort(entry.getKey(), SortOrder.DESC);
            }
        }

        sourceBuilder.query(var1);
        //custom the SearchSourceBuilder setting the subclass can override
        settingSearchSourceBuilder(sourceBuilder);

        searchRequest.source(sourceBuilder);
        //custom the SearchRequest setting the subclass can override
        //this.sortMap.entrySet().

        settingSearchRequest(searchRequest);
        final SearchResponse searchResponse = highLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        final SearchHit[] searchHits = searchResponse.getHits().getHits();
        final List<T> list = Arrays.stream(searchHits).map(hit -> {
            return getEntityMapper().mapToObject(hit.getSourceAsString(), getEntityClass());
        }).collect(Collectors.toList());
        return list;
    }

    private boolean existsDocument(final QueryBuilder var1) throws IOException {
        final List<T> results = (List<T>)search(var1);
        return results.size() > 0;
    }



    protected void settingSearchRequest(final SearchRequest searchRequest) {

    }

    /**
     * SearchSourceBuilder can set other build. such as: sort,filter, highlight, Aggregations, Suggestions, profile. We can see the below link
     * <a>https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high-search.html#java-rest-high-search-request-building-aggs</a>
     * @param sourceBuilder
     */
    protected void settingSearchSourceBuilder(final SearchSourceBuilder sourceBuilder) {
    }


    public void refresh() throws IOException {
        refresh(indexName());
    }

    public void refresh(final String... indices) throws IOException {
        final RefreshRequest request;
        if (indices != null && indices.length > 0) {
            request = new RefreshRequest(indices);
        } else {
            request = new RefreshRequest();
        }

        settingRefreshRequest(request);

        highLevelClient.indices().refresh(request, RequestOptions.DEFAULT);
    }

    protected void settingRefreshRequest(final RefreshRequest request) {
    }

    public abstract Class<T> getEntityClass();

    public T save(final T var1) throws IOException, ConstraintValidationException {
        return index(var1);
    }

    public Iterable<T> saveAll(final Iterable<T> var1) {
        return null;
    }

    public Optional<T> findById(final ID var1) throws IOException {
        final GetRequest getRequest = new GetRequest(indexName(), var1.toString());
        settingGetRequest(getRequest);
        final GetResponse response = highLevelClient.get(getRequest, RequestOptions.DEFAULT);
        final T t = getEntityMapper().mapToObject(response.getSourceAsString(), getEntityClass());
        if (t != null) {
            t.setVersion(response.getVersion());
            settingEsDocument(response, t);
        }
        return Optional.ofNullable(t);
    }

    protected void settingEsDocument(final GetResponse response, final T t) {
    }

    /**
     * customer setting the GetRequest
     * @param getRequest
     */
    protected void settingGetRequest(final GetRequest getRequest) {
    }

    public boolean existsById(final ID var1) throws IOException {
        final GetRequest getRequest = new GetRequest(indexName(), var1.toString());
        getRequest.fetchSourceContext(new FetchSourceContext(false));
        getRequest.storedFields("_none_");
        return highLevelClient.exists(getRequest, RequestOptions.DEFAULT);
    }

    @Override
    public Iterable<T> findAll() throws IOException {
        return search(QueryBuilders.matchAllQuery());
    }

    public Iterable<T> findAllById(final Iterable<ID> var1) {
        return null;
    }

    public long count(final QueryBuilder var1) throws IOException {
        return this.count(var1, indexName());
    }

    public long count(final QueryBuilder var1, final String... indices) throws IOException {
        final CountRequest countRequest = new CountRequest();
        if (indices != null && indices.length > 0) {
            countRequest.indices(indices);
        }
        final SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(var1);
        countRequest.source(searchSourceBuilder);

        settingCountRequest(countRequest);
        final CountResponse countResponse = highLevelClient.count(countRequest, RequestOptions.DEFAULT);

        return countResponse.getCount();
    }

    protected void settingCountRequest(final CountRequest countRequest) {
    }

    public void deleteById(final ID var1) throws IOException {
        final DeleteRequest request = new DeleteRequest(indexName(), var1.toString());
        //setting optional arguments into the deleteRequest
        settingDeleteRequest(request);
        highLevelClient.delete(request, RequestOptions.DEFAULT);
    }

    /**
     * optional arguments
     * @param request
     */
    protected void settingDeleteRequest(final DeleteRequest request) {
    }


    public void delete(final T var1) throws IOException {
        this.deleteById((ID)var1.getId());
    }

    public void deleteAll(final Iterable<? extends T> var1) {

    }

    public void deleteAll() {

    }

    public <E> void update(final E e, final ID id) throws IOException {
        final UpdateRequest request = new UpdateRequest(indexName(), id.toString());

        if (e instanceof String) {
            request.doc((String)e, XContentType.JSON);
        } else if (e instanceof Map) {
            request.doc((Map)e);
        } else if (e instanceof XContentBuilder) {
            request.doc((XContentBuilder)e);
        } else if (e instanceof ElasticsearchDocument) {
            ((ElasticsearchDocument) e).setLastChange(new Date());
            request.doc(getEntityMapper().mapToString((T)e), XContentType.JSON);
        }

        settingUpdateRequest(request);
        highLevelClient.update(request, RequestOptions.DEFAULT);
    }

    protected void settingUpdateRequest(final UpdateRequest request) {
    }

    public boolean createIndex(final Class<T> clazz) throws IOException {

        final Document document = clazz.getAnnotation(Document.class);

        final CreateIndexRequest createIndexRequest = new CreateIndexRequest(document.indexName());
        try {

            IndexSetting indexSetting = document.settingGenerator().newInstance();

            final Object setting = indexSetting.getSetting(document.indexName());

            if (setting instanceof String) {
                createIndexRequest.settings((String)setting, XContentType.JSON);
            } else if (setting instanceof Map) {
                createIndexRequest.settings((Map)setting);
            } else if (setting instanceof XContentBuilder) {
                createIndexRequest.settings((XContentBuilder)setting);
            }

            IndexMapping indexMapping = document.mappingGenerator().newInstance();
            final Object mapping = indexMapping.getMapping(document.indexName());
            if (mapping instanceof String) {
                createIndexRequest.mapping((String)mapping, XContentType.JSON);
            } else if (mapping instanceof Map) {
                createIndexRequest.mapping((Map)mapping);
            } else if (mapping instanceof XContentBuilder) {
                createIndexRequest.mapping((XContentBuilder)mapping);
            }
        } catch (InstantiationException  | IllegalAccessException e) {
            e.printStackTrace();
        }





        return highLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT).isAcknowledged();
    }

    public boolean deleteIndex(final String indexName) throws IOException {
        final DeleteIndexRequest request = new DeleteIndexRequest(indexName);
        return highLevelClient.indices().delete(request, RequestOptions.DEFAULT).isAcknowledged();
    }

    public boolean existIndex(final String indexName) throws IOException {
        final GetIndexRequest request = new GetIndexRequest(indexName);
        settingGetIndexRequest(request);
        return highLevelClient.indices().exists(request, RequestOptions.DEFAULT);
    }

    protected void settingGetIndexRequest(final GetIndexRequest request) {
        request.local(false);
        request.humanReadable(true);
        request.includeDefaults(false);
    }

    public Iterable<T> findByParam(String key,String... values) throws IOException{
        return this.search(QueryBuilders.termsQuery(key, values));
    }


    public EntityMapper<T> getEntityMapper() {
        return mapper;
    }

    public void setEntityMapper(final EntityMapper<T> mapper) {
        this.mapper = mapper;
    }

    public void setSortMap(Map<String, String> sortMap) {
        this.sortMap = sortMap;
    }
    abstract protected String indexName();

    public RestHighLevelClient getRestHighLevelClient() {
        return highLevelClient;
    }
}
