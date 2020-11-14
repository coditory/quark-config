package com.coditory.config;

public interface ConfigValueMapper {
    Object mapValue(String path, Object value);
}
