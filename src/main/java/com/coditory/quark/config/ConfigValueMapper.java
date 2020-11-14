package com.coditory.quark.config;

public interface ConfigValueMapper {
    Object mapValue(String path, Object value);
}
