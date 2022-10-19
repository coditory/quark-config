package com.coditory.quark.config;

public interface ConfigEntryMapper {
    Object mapValue(String path, Object value);
}
