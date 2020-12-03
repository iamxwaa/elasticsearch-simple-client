package org.github.toxrink.elasticsearch.core.entry;

import java.util.Map;

import lombok.Data;

@Data
public class SearchHit {
    private String index;
    private String type;
    private String id;
    private float score;
    private Map<String, Object> source;
}
