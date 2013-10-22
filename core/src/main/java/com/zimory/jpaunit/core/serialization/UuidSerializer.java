package com.zimory.jpaunit.core.serialization;

import java.util.UUID;

public class UuidSerializer extends TypedScalarSerializer<UUID> {

    public UuidSerializer() {
        super(UUID.class);
    }

    @Override
    public String write(final UUID o) {
        return o.toString();
    }

    @Override
    public UUID read(final String s) {
        return UUID.fromString(s);
    }

}