package com.coditory.configio;

public interface ConfigValueMapper {
    Object mapValue(String path, Object value);
}
