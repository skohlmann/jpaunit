package com.zimory.jpaunit.core.it.model;

import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.google.common.base.Objects;

@Entity
public class Unit {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @OneToMany
    private List<Association> associations;

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public List<Association> getAssociations() {
        return associations;
    }

    public void setAssociations(final List<Association> associations) {
        this.associations = associations;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .toString();
    }

}
