package com.daanpanis.core.api.command.meta;

import org.apache.commons.collections4.map.CaseInsensitiveMap;

public class MetaBuilder {

    private final CaseInsensitiveMap<String, Object> metaValues = new CaseInsensitiveMap<>();

    protected MetaBuilder() {
    }

    public MetaBuilder value(String key, Object value) {
        this.metaValues.put(key, value);
        return this;
    }

    public Meta build() {
        return new Meta(metaValues);
    }

}
