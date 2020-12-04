package org.github.toxrink.elasticsearch.core.client;

import java.util.Optional;

import org.github.toxrink.elasticsearch.core.entry.Query;
import org.github.toxrink.elasticsearch.core.entry.Result;

/**
 * 查询操作
 * 
 * @author xw
 * 
 *         2020年12月02日
 */
public interface SearchClient {

    /**
     * es 查询
     * 
     * @param query 查询语句
     * @return
     */
    Optional<Result> search(Query query);

    /**
     * es scroll 查询
     * 
     * @param query 查询语句
     * @return
     */
    Optional<Result> scrollSearch(Query query);
}
