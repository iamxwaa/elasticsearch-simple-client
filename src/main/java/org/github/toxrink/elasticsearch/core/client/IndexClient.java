package org.github.toxrink.elasticsearch.core.client;

import java.util.List;

import org.github.toxrink.elasticsearch.core.entry.Mapping;

/**
 * 索引相关
 * 
 * @author xw
 * 
 *         2020年12月02日
 */
public interface IndexClient {
    /**
     * 获取索引schema
     * 
     * @param index
     * @return
     */
    List<Mapping> getMappings(String index);

    /**
     * 判断索引是否存在
     * 
     * @param index
     * @return
     */
    boolean isIndexExist(String index);
}
