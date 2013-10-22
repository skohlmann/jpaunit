package com.zimory.jpaunit.core;

import com.esotericsoftware.yamlbeans.scalar.ScalarSerializer;
import com.zimory.jpaunit.core.serialization.YamlScalarSerializer;

class Serializers {

    static <T> ScalarSerializer<T> scalarSerializerOf(final YamlScalarSerializer<T> serializer) {
        return new DelegatingScalarSerializer<T>(serializer);
    }

    public static class DelegatingScalarSerializer<T> implements ScalarSerializer<T> {

        private final YamlScalarSerializer<T> serializer;

        public DelegatingScalarSerializer(final YamlScalarSerializer<T> serializer) {
            this.serializer = serializer;
        }

        @Override
        public String write(final T t) {
            return serializer.write(t);
        }

        @Override
        public T read(final String value) {
            return serializer.read(value);
        }

    }

}
