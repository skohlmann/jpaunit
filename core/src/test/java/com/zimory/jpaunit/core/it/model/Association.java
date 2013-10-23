package com.zimory.jpaunit.core.it.model;

import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.google.common.base.Objects;

@Entity
public class Association {

    @Id
    private UUID id;

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .toString();
    }

}
