package org.github.toxrink.elasticsearch.core.client.impl;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Node;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.github.toxrink.elasticsearch.core.client.EsClient;
import org.github.toxrink.elasticsearch.core.entry.ElasticsearchConfig;
import org.github.toxrink.elasticsearch.core.entry.Mapping;
import org.github.toxrink.elasticsearch.core.entry.Query;
import org.github.toxrink.elasticsearch.core.entry.Result;
import org.github.toxrink.elasticsearch.core.entry.SearchHit;

import lombok.extern.slf4j.Slf4j;

/**
 * 默认的es客户端实现
 * 
 * @author xw
 * 
 *         2020年12月02日
 */
@Slf4j
public class DefaultEsClientImpl extends EsClient {

    private RestClient restClient;

    private AtomicInteger batchCount = new AtomicInteger(0);

    private StringBuffer dataBuffer;

    private ReentrantLock lock = new ReentrantLock();

    public static String DEFAULT_TIME_VALUE = "600s";

    public DefaultEsClientImpl(ElasticsearchConfig config) {
        super(config);
        log.info("Use default rest client for cluster " + config.getClusterName());
        this.restClient = buildRestClient(config);
    }

    public DefaultEsClientImpl(RestClient restClient) {
        super(null);
        log.info("Use {} as rest client", restClient.getClass().getName());
        this.restClient = restClient;
    }

    private RestClient buildRestClient(ElasticsearchConfig config) {
        Set<String> ipSet = new HashSet<>();
        if (StringUtils.isNotEmpty(config.getMaster())) {
            ipSet.add(config.getMaster());
        }
        for (String ip : config.getClusterIp()) {
            ipSet.add(ip);
        }
        String schema = config.isHttpSslEnabled() ? "https" : "http";
        RestClientBuilder builder = RestClient
                .builder(ipSet.stream().map(ip -> new HttpHost(ip, config.getPort(), schema))
                        .collect(Collectors.toList()).toArray(new HttpHost[0]));

        // 服务认证
        if (StringUtils.isNotEmpty(config.getUserName()) && StringUtils.isNotEmpty(config.getPassword())) {
            log.info("Enable basic authorization.");
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(config.getUserName(), config.getPassword()));
            builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                @Override
                public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
                    httpClientBuilder.disableAuthCaching();
                    if (config.isHttpSslEnabled()) {
                        log.info("Enable https client.");
                        try {
                            TrustStrategy trustStrategy = new TrustStrategy() {
                                public boolean isTrusted(X509Certificate[] arg0, String arg1)
                                        throws CertificateException {
                                    return true;
                                }
                            };
                            SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, trustStrategy)
                                    .build();
                            httpClientBuilder.setSSLContext(sslContext);
                            httpClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
                        } catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException e) {
                            log.error("", e);
                        }
                    }
                    return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                }
            });
        }
        builder = builder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            @Override
            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                return requestConfigBuilder.setConnectTimeout(config.getConnectTimeout())
                        .setSocketTimeout(config.getSocketTimeout())
                        .setConnectionRequestTimeout(config.getConnectionRequestTimeout());
            }
        });
        builder.setFailureListener(new RestClient.FailureListener() {
            @Override
            public void onFailure(Node node) {
                log.error("{}://{}:{} connect error.", new Object[] { node.getHost().getSchemeName(),
                        node.getHost().getHostName(), node.getHost().getPort() });
            }
        });
        return builder.build();
    }

    @Override
    public Optional<Result> search(Query query) {
        if (query.getTimeValue() != 0 || StringUtils.isNotBlank(query.getScrollId())) {
            return scrollSearch(query);
        }
        String endpoint = getQueryEndpoint(query);
        Request request = new Request(POST, endpoint);
        setDefaultParameter(request);

        StringBuilder queryBuilder = new StringBuilder().append("\"from\":").append(query.getFrom()).append(",")
                .append("\"size\":").append(query.getSize());

        String queryString = StringUtils.isEmpty(query.getQuery())
                ? new StringBuilder("{").append(queryBuilder).append("}").toString()
                : new StringBuilder(query.getQuery()).insert(1, queryBuilder.append(",")).toString();

        request.setJsonEntity(queryString);
        try {
            Response response = restClient.performRequest(request);
            return Optional.ofNullable(parseEntity(response.getEntity()));
        } catch (IOException e) {
            log.error("Search request error.", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Result> scrollSearch(Query query) {
        Result result = null;
        if (StringUtils.isEmpty(query.getScrollId())) {
            String endpoint = getQueryEndpoint(query);
            Request request = new Request(POST, endpoint);
            setDefaultParameter(request);
            request.addParameter("scroll", query.getTimeValue() == 0 ? DEFAULT_TIME_VALUE : query.getTimeValue() + "s");

            StringBuilder queryBuilder = new StringBuilder().append("\"from\":").append(query.getFrom()).append(",")
                    .append("\"size\":").append(query.getSize());

            String queryString = StringUtils.isEmpty(query.getQuery())
                    ? new StringBuilder("{").append(queryBuilder).append("}").toString()
                    : new StringBuilder(query.getQuery()).insert(1, queryBuilder.append(",")).toString();

            request.setJsonEntity(queryString);
            try {
                Response response = restClient.performRequest(request);
                result = parseEntity(response.getEntity());
            } catch (IOException e) {
                log.error("Scroll search error.", e);
            }
        } else {
            Request request = new Request(POST, "/_search/scroll");
            String time = query.getTimeValue() == 0 ? DEFAULT_TIME_VALUE : query.getTimeValue() + "s";
            String id = query.getScrollId();
            request.setJsonEntity(new StringBuilder("{\"scroll_id\":\"").append(id).append("\",\"scroll\":\"")
                    .append(time).append("\"}").toString());
            try {
                Response response = restClient.performRequest(request);
                result = parseEntity(response.getEntity());
            } catch (IOException e) {
                log.error("Next scroll search error.", e);
            }
        }
        if (null != result) {
            query.setScrollId(result.getScrollId());
        }
        return Optional.ofNullable(result);
    }

    private String getQueryEndpoint(Query query) {
        StringBuilder endpoint = new StringBuilder("/");
        String index = String.join(",", query.getIndecies());
        String type = (null == query.getTypes() || query.getTypes().length == 0) ? ""
                : String.join(",", query.getTypes());

        if (StringUtils.isEmpty(type)) {
            endpoint.append(index);
        } else {
            endpoint.append(index).append("/").append(type);
        }
        endpoint.append("/_search");
        return endpoint.toString();
    }

    private void setDefaultParameter(Request request) {
        request.addParameter("typed_keys", "true");
        request.addParameter("ignore_unavailable", "false");
        request.addParameter("expand_wildcards", "open");
        request.addParameter("allow_no_indices", "true");
        request.addParameter("search_type", "query_then_fetch");
        request.addParameter("batched_reduce_size", "512");
    }

    private Result parseEntity(HttpEntity httpEntity) throws IOException {
        JSONObject jsonObject = JSON.parseObject(IOUtils.toByteArray(httpEntity.getContent()), JSONObject.class);
        Result result = new Result();
        result.setTimeOut(jsonObject.getBooleanValue("timed_out"));
        result.setTook(jsonObject.getLongValue("took"));
        result.setScrollId(jsonObject.getString("_scroll_id"));
        JSONObject hitObj = jsonObject.getJSONObject("hits");
        if (null == hitObj) {
            result.setEmpty(true);
        } else {
            JSONArray hits = hitObj.getJSONArray("hits");
            result.setEmpty(hits.isEmpty());
            if (hits.isEmpty()) {
                result.setTotal(0);
                result.setHits(new SearchHit[0]);
            } else {
                result.setTotal(hitObj.getLongValue("total"));
                result.setHits(hits.parallelStream().map(hit -> {
                    JSONObject hit2 = (JSONObject) hit;
                    SearchHit searchHit = new SearchHit();
                    searchHit.setId(hit2.getString("_id"));
                    searchHit.setIndex(hit2.getString("_index"));
                    searchHit.setScore(hit2.getFloatValue("_score"));
                    searchHit.setType(hit2.getString("_type"));
                    searchHit.setSource(hit2.getJSONObject("_source"));
                    return searchHit;
                }).toArray(SearchHit[]::new));
            }
        }
        return result;
    }

    @Override
    public synchronized int addBatch(String index, String type, String id, Map<String, Object> source) {
        dataBuffer.append("{\"index\":{\"_index\":\"");
        dataBuffer.append(index);
        dataBuffer.append("\",\"_type\":\"");
        dataBuffer.append(type);
        if (StringUtils.isNotEmpty(id)) {
            dataBuffer.append("\",\"_id\":\"");
            dataBuffer.append(id);
        }
        dataBuffer.append("\"}}");
        dataBuffer.append("\n");
        dataBuffer.append(toJSONString(source));
        dataBuffer.append("\n");
        return batchCount.incrementAndGet();
    }

    @Override
    public void newBatch() {
        dataBuffer = new StringBuffer();
        batchCount.set(0);
    }

    @Override
    public int executeBatch() {
        int count = batchCount();
        try {
            lock.lock();
            StringBuffer tmp = this.dataBuffer;
            this.newBatch();
            if (null != tmp) {
                bulk(tmp);
            }
        } catch (Exception e) {
            log.error("", e);
        } finally {
            lock.unlock();
        }
        return count;
    }

    @Override
    public int batchCount() {
        return batchCount.get();
    }

    @Override
    public boolean delete(String index, String type, String id) {
        Response response = null;
        try {
            response = restClient.performRequest(buildRequest(DELETE,
                    new StringBuilder("/").append(index).append("/").append(type).append("/").append(id).toString()));
            if (log.isDebugEnabled()) {
                log.debug("Delete response entity is : " + EntityUtils.toString(response.getEntity()));
            }
            return true;
        } catch (Exception e) {
            log.error("Delete failed, exception occurred.", e);
        }
        return false;
    }

    @Override
    public boolean update(String index, String type, String id, Map<String, Object> source) {
        String jsonString = toJSONString(source);
        HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
        Response response = null;
        try {
            response = restClient.performRequest(buildRequest(POST,
                    new StringBuilder("/").append(index).append("/").append(type).append("/").append(id).toString(),
                    entity));
            if (log.isDebugEnabled()) {
                log.debug("Update response entity is : " + EntityUtils.toString(response.getEntity()));
            }
            return true;
        } catch (Exception e) {
            log.error("Update failed, exception occurred.", e);
        }
        return false;
    }

    @Override
    public boolean put(String index, String type, Map<String, Object> source) {
        String jsonString = toJSONString(source);
        HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
        Response response = null;
        try {
            response = restClient.performRequest(buildRequest(PUT,
                    new StringBuilder("/").append(index).append("/").append(type).toString(), entity));
            if (log.isDebugEnabled()) {
                log.debug("Put response entity is : " + EntityUtils.toString(response.getEntity()));
            }
            return true;
        } catch (Exception e) {
            log.error("Put failed, exception occurred.", e);
        }
        return false;
    }

    @Override
    public List<Mapping> getMappings(String index) {
        List<Mapping> list = new ArrayList<>(1);
        try {
            Response resp = restClient.performRequest(
                    buildRequest(GET, new StringBuilder("/").append(index).append("/_mappings").toString()));
            Map<String, Map<String, Object>> map = JSON.parseObject(IOUtils.toByteArray(resp.getEntity().getContent()),
                    LinkedHashMap.class);
            map.forEach((k, v) -> {
                Mapping mapping = new Mapping();
                mapping.setIndex(k);
                ((JSONObject) v.get("mappings")).forEach((k2, v2) -> {
                    mapping.setType(k2);
                    Map<String, String> typeMap = new HashMap<>();
                    ((JSONObject) v2).getJSONObject("properties").forEach((k3, v3) -> {
                        typeMap.put(k3, ((JSONObject) v3).getString("type"));
                    });
                    mapping.setFieldType(typeMap);
                });
                list.add(mapping);
            });
        } catch (IOException e) {
            log.error("", e);
        }
        return list;
    }

    @Override
    public boolean isIndexExist(String index) {
        Response response = null;
        try {
            response = restClient.performRequest(buildRequest(HEAD, "/" + index));
            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                if (log.isDebugEnabled()) {
                    log.debug("Check index successful,index is exist : " + index);
                }
                return true;
            }
            if (HttpStatus.SC_NOT_FOUND == response.getStatusLine().getStatusCode()) {
                if (log.isDebugEnabled()) {
                    log.debug("Index is not exist : " + index);
                }
                return false;
            }
        } catch (Exception e) {
            log.error("Check index failed, exception occurred.", e);
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        restClient.close();
    }

    private void bulk(StringBuffer bufferData) {
        StringEntity entity = new StringEntity(bufferData.toString(), ContentType.APPLICATION_JSON);
        Response response = null;
        try {
            response = restClient.performRequest(buildRequest(PUT, "/_bulk", entity));
            if (log.isDebugEnabled()) {
                log.debug("Bulk response entity is : " + EntityUtils.toString(response.getEntity()));
            }
        } catch (Exception e) {
            log.error("Bulk failed, exception occurred.", e);
        }
    }

    private Request buildRequest(String method, String endpoint) {
        return buildRequest(method, endpoint, null);
    }

    private Request buildRequest(String method, String endpoint, HttpEntity entity) {
        Request request = new Request(method, endpoint);
        if (null != entity) {
            request.setEntity(entity);
        }
        return request;
    }
}
