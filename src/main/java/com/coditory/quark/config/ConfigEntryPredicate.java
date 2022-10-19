package com.coditory.quark.config;

public interface ConfigEntryPredicate {
    boolean test(String path, Object value);
}
