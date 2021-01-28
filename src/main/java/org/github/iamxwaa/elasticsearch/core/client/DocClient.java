package org.github.iamxwaa.elasticsearch.core.client;

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
     * @param id     数据主键，为null时自动生成
     * @param source 数据
     * @return 当前添加的数据条数
     */
    int addBatch(String index, String type, String id, Map<String, Object> source);

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
     * @param index 索引
     * @param type  类型
     * @param id    id
     * @return 是否删除成功
     */
    boolean delete(String index, String type, String id);

    /**
     * 修改es中的数据
     * 
     * @param index   索引
     * @param type    类型
     * @param id      id
     * @param field   字段
     * @param content 修改内容
     * @return 是否修改成功
     */
    boolean update(String index, String type, String id, Map<String, Object> source);

    /**
     * 新增数据到es
     * 
     * @param index 索引
     * @param type  类型
     * @param data  数据
     * @return 是否添加成功
     */
    boolean put(String index, String type, Map<String, Object> source);

}
