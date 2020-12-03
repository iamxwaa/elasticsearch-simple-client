package org.github.toxrink.elasticsearch.core.client;

import java.util.Map;

/**
 * 文档操作
 * 
 * @author xw
 * 
 *         2020年12月02日
 */
public interface DocClient {
    /**
     * 添加数据到bulk request,首次添加需要先调用newBatch,实现时建议添加synchronized
     * 
     * @param index  索引名称
     * @param type   类型
     * @param source 数据
     * @return
     */
    int addBatch(String index, String type, Map<String, Object> source);

    /**
     * 新建一个es的bulk request,实现时建议添加synchronized
     */
    void newBatch();

    /**
     * 提交bulk request,实现时建议添加synchronized
     * 
     * @return 返回提交条数
     */
    int executeBatch();

    /**
     * 提交bulk request,当未达到指定batch时不会提交数据,实现时建议添加synchronized
     * 
     * @param batch 每次提交的批次
     * @return 返回提交条数
     */
    int executeBatch(int batch);

    /**
     * 已添加的bulk request数量
     * 
     * @return
     */
    int batchCount();

    /**
     * 最后一次提交
     * 
     * @return 返回提交条数
     */
    int executeLastBatch();

    /**
     * 最后一次提交,当未达到指定batch时不会提交数据
     * 
     * @return 返回提交条数
     */
    int executeLastBatch(int batch);

    /**
     * 删除es中的数据
     * 
     * @param index
     * @param type
     * @param id
     * @return
     */
    boolean delete(String index, String type, String id);

    /**
     * 修改es中的数据
     * 
     * @param index
     * @param type
     * @param id
     * @param field
     * @param content
     * @return
     */
    boolean update(String index, String type, String id, Map<String, Object> source);

    /**
     * 新增数据到es
     * 
     * @param index
     * @param type
     * @param data
     * @return
     */
    boolean put(String index, String type, Map<String, Object> source);

}
