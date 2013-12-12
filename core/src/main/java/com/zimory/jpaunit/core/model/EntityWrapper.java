package com.zimory.jpaunit.core.model;

import java.util.Set;

import javax.persistence.PersistenceUnitUtil;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

public final class EntityWrapper {

    private final PersistenceUnitUtil util;
    private final Object entity;

    private EntityWrapper(final PersistenceUnitUtil util, final Object entity) {
        this.util = util;
        this.entity = entity;
    }

    public Object getEntity() {
        return entity;
    }

    public Object getEntityId() {
        return util.getIdentifier(entity);
    }

    public Class<?> getEntityClass() {
        return entity.getClass();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getEntityId());
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        final EntityWrapper other = (EntityWrapper) obj;

        return getEntityClass() == other.getEntityClass() && Objects.equal(getEntityId(), other.getEntityId());
    }

    public static Set<EntityWrapper> wrap(final PersistenceUnitUtil util, final Set<Object> entities) {
        return Sets.newHashSet(Collections2.transform(entities, new WrapperFunction(util)));
    }

    public static EntityWrapper wrap(final PersistenceUnitUtil util, final Object entity) {
        return new EntityWrapper(util, entity);
    }

    private static class WrapperFunction implements Function<Object, EntityWrapper> {

        private final PersistenceUnitUtil util;

        private WrapperFunction(final PersistenceUnitUtil util) {
            this.util = util;
        }

        @Override
        public EntityWrapper apply(final Object input) {
            return wrap(util, input);
        }

    }

}
