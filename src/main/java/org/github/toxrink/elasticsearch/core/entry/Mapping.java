package org.github.toxrink.elasticsearch.core.entry;

import java.util.Map;

import lombok.Data;
import lombok.ToString;

/**
 * 索引模板
 * 
 * @author xw
 * 
 *         2020年12月02日
 */
@Data
@ToString
public class Mapping {
    private String index;

    private String type;

    private Map<String, String> fieldType;
}
