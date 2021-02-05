package org.github.iamxwaa.elasticsearch.utils;

import java.util.List;

import org.github.iamxwaa.elasticsearch.constants.ClusterConst;
import org.github.iamxwaa.elasticsearch.core.entry.ElasticsearchConfig;
import org.github.iamxwaa.jxwrapper2.bean.BeanBuilders;
import org.github.iamxwaa.jxwrapper2.yaml.YamlBuilders;
import org.github.iamxwaa.jxwrapper2.yaml.YamlEntry;

public class ConfigUtils {

    public static void main(String[] args) {
        String path = "D:\\Downloads\\elasticsearch-5.5.3\\config\\elasticsearch.yml";
        long start = System.currentTimeMillis();
        System.out.println(loadConfig(path));
        System.out.println(System.currentTimeMillis() - start);
    }

    /**
     * 读取elasticsearch.yml
     * 
     * @param path 文件路径
     * @return
     */
    public static ElasticsearchConfig loadConfig(String path) {
        YamlEntry yamlEntry = YamlBuilders.builder().path(path).build();
        List<String> tmp = yamlEntry.getStringList(ClusterConst.CLUSTER_IPS);
        if (null != tmp) {
            yamlEntry.originYaml().put(ClusterConst.CLUSTER_IPS, String.join(",", tmp));
        }
        ElasticsearchConfig config = new ElasticsearchConfig();
        config.setConfigPath(path);
        BeanBuilders.buildSetter(config).build().injectConfigValue(yamlEntry.originYaml());
        return config;
    }
}
