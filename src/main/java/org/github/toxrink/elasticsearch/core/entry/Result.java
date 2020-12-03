package org.github.toxrink.elasticsearch.core.entry;

import lombok.Data;

/**
 * 查询结果
 * 
 * @author xw
 * 
 *         2020年12月02日
 */
@Data
public class Result {
    /**
     * 查询结果条数
     */
    private long total;

    /**
     * 查询的数据
     */
    private SearchHit[] hits;

    /**
     * scrolld id
     */
    private String scrollId;

    /**
     * 结果是否为空
     */
    private boolean empty;

    /**
     * 是否超时
     */
    private boolean timeOut;

    /**
     * 耗时
     */
    private long took;

    @Override
    public String toString() {
        return "Result [empty=" + empty + ", hits= {...}, scrollId=" + scrollId + ", timeOut=" + timeOut + ", took="
                + took + ", total=" + total + "]";
    }

}