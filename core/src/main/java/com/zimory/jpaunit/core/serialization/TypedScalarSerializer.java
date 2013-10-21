package com.zimory.jpaunit.core.serialization;

public abstract class TypedScalarSerializer<T> implements YamlScalarSerializer<T> {

    private final Class<T> type;

    public TypedScalarSerializer(final Class<T> type) {
        this.type = type;
    }

    @Override
    public Class<T> getType() {
        return type;
    }

}
