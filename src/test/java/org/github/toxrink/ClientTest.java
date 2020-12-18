package org.github.toxrink;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;

import org.github.toxrink.elasticsearch.EsClientFactory;
import org.github.toxrink.elasticsearch.constants.ClusterConst;
import org.github.toxrink.elasticsearch.core.client.EsClient;
import org.github.toxrink.elasticsearch.core.entry.ElasticsearchConfig;
import org.github.toxrink.elasticsearch.core.entry.Query;
import org.github.toxrink.elasticsearch.core.entry.Result;
import org.github.toxrink.elasticsearch.utils.ConfigUtils;

import x.utils.ReflectUtils;

public class ClientTest {
    public static void main(String[] args) throws IOException {
        String path = "C:\\Users\\admin\\Desktop\\test\\elasticsearch.yml";
        ElasticsearchConfig config = ConfigUtils.loadConfig(path);
        config.setPassword("vrv@12345");
        config.setUserName("admin");
        ReflectUtils.printConfigValue(config);
        EsClient client = EsClientFactory.getEsClient(config);
        String index = "app-audit-2020.10";
        System.out.println(client.isIndexExist(index));
        Query query = new Query();
        query.setFrom(0);
        query.setIndecies(index);
        query.setSize(10);
        query.setQuery("{\"_source\":[\"username\"]}");
        // query.setTimeValue(6000);
        Optional<Result> rOptional = client.search(query);
        if (rOptional.isPresent()) {
            System.out.println(rOptional);
        }
        client.newBatch();
        index = "hahatest";
        String type = "logs";
        Map<String, Object> source = new HashMap<>();
        Date t = new Date();
        String id = t.getTime() + "";
        source.put("msg", "21djfksdflj");
        source.put("time", t);
        client.addBatch(index, type, id, source);
        client.addBatch(index, type, null, source);
        client.executeLastBatch();
        client.delete(index, type, "1608273072662");
        client.close();
    }
}
