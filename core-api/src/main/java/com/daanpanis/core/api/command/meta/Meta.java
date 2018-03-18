package com.daanpanis.core.api.command.meta;

import org.apache.commons.collections4.map.CaseInsensitiveMap;

public class Meta {

    public static MetaBuilder builder() {
        return new MetaBuilder();
    }

    public static Meta empty() {
        return new Meta(new CaseInsensitiveMap<>(0));
    }

    private final CaseInsensitiveMap<String, Object> metaValues;

    public Meta(CaseInsensitiveMap<String, Object> metaValues) {
        this.metaValues = metaValues;
    }

    public boolean has(String key) {
        return metaValues.containsKey(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) metaValues.get(key);
    }

    public <T> T get(String key, Class<T> castTo) {
        return get(key);
    }

    public boolean is(String key, Object valueToMatch) {
        if (!has(key)) return false;
        String value = get(key);
        return value.equals(valueToMatch);
    }
}
