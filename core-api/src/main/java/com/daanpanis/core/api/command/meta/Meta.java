package com.daanpanis.core.api.command.meta;

import org.apache.commons.collections4.map.CaseInsensitiveMap;

public class Meta {

    public static MetaBuilder builder() {
        return new MetaBuilder();
    }

    public static Meta empty() {
        return new Meta(new CaseInsensitiveMap<>(0));
    }

    private final CaseInsensitiveMap<String, String> metaValues;

    public Meta(CaseInsensitiveMap<String, String> metaValues) {
        this.metaValues = metaValues;
    }

    public boolean has(String key) {
        return metaValues.containsKey(key);
    }

    public String get(String key) {
        return metaValues.get(key);
    }

    public boolean is(String key, String value) {
        return is(key, value, true);
    }

    public boolean is(String key, String valueToMatch, boolean matchCase) {
        if (!has(key))
            return false;
        String value = get(key);
        return matchCase ? value.equals(valueToMatch) : value.equalsIgnoreCase(valueToMatch);
    }
}
