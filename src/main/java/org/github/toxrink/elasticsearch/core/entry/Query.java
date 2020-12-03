package org.github.toxrink.elasticsearch.core.entry;

import lombok.Data;
import lombok.ToString;

/**
 * 查询条件
 * 
 * @author xw
 * 
 *         2020年12月02日
 */
@Data
@ToString
public class Query {
    /**
     * 索引名称
     */
    private String[] indecies;

    /**
     * 数据类型
     */
    private String[] types;

    /**
     * es查询语句
     */
    private String query;

    /**
     * scroll 查询缓存时间(scroll查询需设置)
     */
    private int timeValue;

    /**
     * scroll 查询id
     */
    private String scrollId;

    /**
     * 其起始位置
     */
    private int from;

    /**
     * 分页大小
     */
    private int size = 1000;

    public void setIndecies(String... indecies) {
        this.indecies = indecies;
    }

    public void setTypes(String... types) {
        this.types = types;
    }

}
