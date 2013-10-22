package com.zimory.jpaunit.core;

import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

final class EntityWrapper {

    private final Dao dao;
    private final Object entity;

    private EntityWrapper(final Dao dao, final Object entity) {
        this.dao = dao;
        this.entity = entity;
    }

    public Object getEntity() {
        return entity;
    }

    private Object getEntityId() {
        return dao.getIdFor(entity);
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

        return Objects.equal(getEntityId(), other.getEntityId());
    }

    public static Set<EntityWrapper> wrap(final Dao dao, final Set<Object> entities) {
        return Sets.newHashSet(Collections2.transform(entities, new WrapperFunction(dao)));
    }

    public static EntityWrapper wrap(final Dao dao, final Object entity) {
        return new EntityWrapper(dao, entity);
    }

    private static class WrapperFunction implements Function<Object, EntityWrapper> {

        private final Dao dao;

        private WrapperFunction(final Dao dao) {
            this.dao = dao;
        }

        @Override
        public EntityWrapper apply(final Object input) {
            return wrap(dao, input);
        }

    }

}
