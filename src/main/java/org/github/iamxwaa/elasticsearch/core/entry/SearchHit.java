package org.github.iamxwaa.elasticsearch.core.entry;

import java.util.Map;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class SearchHit {
    private String index;
    private String type;
    private String id;
    private float score;
    private Map<String, Object> source;
}
