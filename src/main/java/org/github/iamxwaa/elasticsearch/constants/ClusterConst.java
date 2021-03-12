package org.github.iamxwaa.elasticsearch.constants;

import java.util.TimeZone;

public interface ClusterConst {

    /**
     * GET请求
     */
    public static final String GET = "GET";

    /**
     * PUT请求
     */
    public static final String PUT = "PUT";

    /**
     * POST请求
     */
    public static final String POST = "POST";

    /**
     * DELETE请求
     */
    public static final String DELETE = "DELETE";

    /**
     * HEAD请求
     */
    public static final String HEAD = "HEAD";

    /**
     * 集群名称
     */
    public static final String CLUSTER_NAME = "cluster.name";

    /**
     * 集群ip列表
     */
    public static final String CLUSTER_IPS = "discovery.zen.ping.unicast.hosts";

    /**
     * 主节点ip
     */
    public static final String MASTER_IP = "network.host";

    /**
     * 连接端口
     */
    public static final String PORT = "http.port";
    /**
     * 默认端口
     */
    public static final String PORT_DEFAULT = "9200";

    /**
     * true => https or false => http
     */
    public static final String ITOOLS_SECURITY_SSLONLY = "itools.security.http.ssl.enabled";

    /**
     * default: TLSv1.2
     */
    public static final String ITOOLS_SECURITY_PROTOCOL = "itools.security.http.ssl.protocol";

    /**
     * 认证用户名
     */
    public static final String USER_NAME = "itools.security.basic.username";

    /**
     * 认证密码
     */
    public static final String PASSWORD = "itools.security.basic.password";

    public static final String REST_REQUEST_TIMEOUT = "rest.connection.request.timeout";
    public static final String REST_REQUEST_TIMEOUT_DEFAULT = "30000";

    public static final String REST_CONNECT_TIMEOUT = "rest.connect.timeout";
    public static final String REST_CONNECT_TIMEOUT_DEFAULT = "5000";

    public static final String REST_SOCKET_TIMEOUT = "rest.socket.timeout";
    public static final String REST_SOCKET_TIMEOUT_DEFAULT = "60000";

    public static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");
    public static final String UTC_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
}
