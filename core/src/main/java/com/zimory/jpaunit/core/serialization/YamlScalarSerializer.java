package com.zimory.jpaunit.core.serialization;

public interface YamlScalarSerializer<T> {

    Class<T> getType();

    String write(T t);

    T read(String value);

}
