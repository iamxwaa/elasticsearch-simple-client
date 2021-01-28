package org.github.iamxwaa.elasticsearch.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.github.iamxwaa.elasticsearch.constants.ClusterConst;
import org.github.iamxwaa.elasticsearch.core.entry.ElasticsearchConfig;

import x.utils.ReflectUtils;
import x.yaml.YamlWrapper;

public class ConfigUtils {

    // public static void main(String[] args) {
    //     String path = "D:\\Downloads\\elasticsearch-5.5.3\\config\\elasticsearch.yml";
    //     System.out.println(loadConfig(path));
    // }

    /**
     * 读取elasticsearch.yml
     * 
     * @param path 文件路径
     * @return
     */
    public static ElasticsearchConfig loadConfig(String path) {
        LinkedHashMap<String, Object> yaml = YamlWrapper.loadYamlAsLinkedHashMap(path);
        @SuppressWarnings("unchecked")
        ArrayList<String> tmp = (ArrayList<String>) yaml.get(ClusterConst.CLUSTER_IPS);
        if (null != tmp) {
            yaml.put(ClusterConst.CLUSTER_IPS, String.join(",", tmp));
        }
        ElasticsearchConfig config = new ElasticsearchConfig();
        config.setConfigPath(path);
        ReflectUtils.wrapObject(config).injectConfigValue(yaml);
        return config;
    }
}
