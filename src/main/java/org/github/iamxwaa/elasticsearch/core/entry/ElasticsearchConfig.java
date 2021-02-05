package org.github.iamxwaa.elasticsearch.core.entry;

import org.github.iamxwaa.elasticsearch.constants.ClusterConst;
import org.github.iamxwaa.jxwrapper2.bean.ConfigValue;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ElasticsearchConfig {
    @ConfigValue(alias = ClusterConst.CLUSTER_NAME)
    private String clusterName;

    @ConfigValue(alias = ClusterConst.CLUSTER_IPS)
    private String[] clusterIp;

    @ConfigValue(alias = ClusterConst.MASTER_IP)
    private String master;

    @ConfigValue(alias = ClusterConst.PORT)
    private int port;

    @ConfigValue(alias = ClusterConst.ITOOLS_SECURITY_SSLONLY)
    private boolean httpSslEnabled;

    @ConfigValue(alias = ClusterConst.ITOOLS_SECURITY_PROTOCOL)
    private String httpSslProtocol;

    @ConfigValue(alias = ClusterConst.USER_NAME)
    private String userName;

    @ConfigValue(alias = ClusterConst.PASSWORD)
    private String password;

    @ConfigValue(alias = ClusterConst.REST_CONNECT_TIMEOUT, value = ClusterConst.REST_CONNECT_TIMEOUT_DEFAULT)
    private int connectTimeout;

    @ConfigValue(alias = ClusterConst.REST_SOCKET_TIMEOUT, value = ClusterConst.REST_SOCKET_TIMEOUT_DEFAULT)
    private int socketTimeout;

    @ConfigValue(alias = ClusterConst.REST_REQUEST_TIMEOUT, value = ClusterConst.REST_REQUEST_TIMEOUT_DEFAULT)
    private int connectionRequestTimeout;

    private String configPath;
}