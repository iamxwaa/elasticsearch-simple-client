package org.github.toxrink.elasticsearch;

import org.elasticsearch.client.RestClient;
import org.github.toxrink.elasticsearch.core.client.EsClient;
import org.github.toxrink.elasticsearch.core.client.impl.DefaultEsClientImpl;
import org.github.toxrink.elasticsearch.utils.ConfigUtils;
import org.github.toxrink.elasticsearch.core.entry.ElasticsearchConfig;

/**
 * 获取es连接客户端
 * 
 * @author xw
 * 
 *         2020年12月02日
 */
public class EsClientFactory {

    /**
     * 获取es客户端
     * 
     * @param path elasticsearch.yml配置路径
     * @return
     */
    public static EsClient getEsClient(String path) {
        ElasticsearchConfig config = ConfigUtils.loadConfig(path);
        return getEsClient(config);
    }

    /**
     * 获取es客户端
     * 
     * @param config elasticsearch配置
     * @return
     */
    public static EsClient getEsClient(ElasticsearchConfig config) {
        return new DefaultEsClientImpl(config);
    }

    /**
     * 获取es客户端
     * 
     * @param restClient es rest client
     * @return
     */
    public static EsClient getEsClient(RestClient restClient) {
        return new DefaultEsClientImpl(restClient);
    }

    /**
     * 获取es客户端
     * 
     * @param path     配置路径
     * @param userName 认证用户
     * @param password 认证密码
     * @return
     */
    public static EsClient getClientByEsymlWithAuthorization(String path, String userName, String password) {
        ElasticsearchConfig config = ConfigUtils.loadConfig(path);
        config.setUserName(userName);
        config.setPassword(password);
        return getEsClient(config);
    }
}
