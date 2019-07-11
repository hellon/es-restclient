package com.liuvlun.es.storage.base;

import java.io.IOException;
import java.util.Optional;

import org.elasticsearch.index.query.QueryBuilder;

/**
 * Created by liuhailo on 2019/6/18.
 */
public interface ESCrudRepository<T, ID> extends ESRepository<T> {
    T index(T var1) throws IOException, ConstraintValidationException;

    T save(T var1) throws IOException, ConstraintValidationException;

    Iterable<T> saveAll(Iterable<T> var1);

    Optional<T> findById(ID var1) throws IOException;

    boolean existsById(ID var1) throws IOException;

    Iterable<T> findAll() throws IOException;

    Iterable<T> findAllById(Iterable<ID> var1);

    long count(QueryBuilder var1) throws IOException;

    long count(QueryBuilder var1, String... indices) throws IOException;

    void deleteById(ID var1) throws IOException;

    void delete(T var1) throws IOException;

    void deleteAll(Iterable<? extends T> var1);

    void deleteAll();

    boolean createIndex(Class<T> clazz) throws IOException;

    boolean deleteIndex(String indexName) throws IOException;

    boolean existIndex(String indexName) throws IOException;

    <E> void update(E e, ID id) throws IOException;

    Iterable<T> findByParam(String key,String... values) throws IOException;
}
