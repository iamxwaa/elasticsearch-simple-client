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
     * 获取索引模板配置
     * 
     * @param index 目标索引
     * @return 模板配置
     */
    List<Mapping> getMappings(String index);

    /**
     * 判断索引是否存在
     * 
     * @param index 目标索引
     * @return 是否存在
     */
    boolean isIndexExist(String index);
}
