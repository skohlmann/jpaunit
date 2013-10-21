package com.zimory.jpaunit.core;

import com.esotericsoftware.yamlbeans.scalar.ScalarSerializer;
import com.zimory.jpaunit.core.serialization.YamlScalarSerializer;

class Serializers {

    static <T> ScalarSerializer<T> scalarSerializerOf(final YamlScalarSerializer<T> serializer) {
        return new ScalarSerializer<T>() {
            @Override
            public String write(final T t) {
                return serializer.write(t);
            }
            @Override
            public T read(final String value) {
                return serializer.read(value);
            }
        };
    }

}
