package com.zimory.jpaunit.core;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.EntityType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSortedSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Dao {

    private static final Logger LOGGER = LoggerFactory.getLogger(Dao.class);

    private final EntityManager em;
    private final Comparator<Class<?>> entityTypeOrdering;

    public Dao(final EntityManager em, final Comparator<Class<?>> entityTypeOrdering) {
        this.em = em;
        this.entityTypeOrdering = entityTypeOrdering;
    }

    public <T> List<T> findAll(final Class<T> javaType) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<T> cq = cb.createQuery(javaType);

        cq.from(javaType);

        return em.createQuery(cq).getResultList();
    }

    public List<Object> findAll() {
        final Builder<Object> builder = ImmutableList.builder();

        for (final EntityType<?> e : getEntityTypes()) {
            builder.addAll(findAll(e.getJavaType()));
        }

        return builder.build();
    }

    private Set<EntityType<?>> getEntityTypes() {
        final Set<EntityType<?>> unsortedEntityTypes = em.getEntityManagerFactory().getMetamodel().getEntities();

        if (entityTypeOrdering == null) {
            return unsortedEntityTypes;
        }

        return ImmutableSortedSet.copyOf(new Comparator<EntityType<?>>() {
            @Override
            public int compare(final EntityType<?> o1, final EntityType<?> o2) {
                return entityTypeOrdering.compare(o1.getJavaType(), o2.getJavaType());
            }
        }, unsortedEntityTypes);
    }

    public <T> T find(final Class<T> entityClass, final Object id) {
        return em.find(entityClass, id);
    }

    public void persist(final Collection<Object> entities) {
        withTransaction(new ExecuteInTransaction() {
            @Override
            public void execute() {
                for (final Object entity : entities) {
                    LOGGER.debug("Persisting entity: {}", entity);

                    persist(entity);
                }
            }
        });
    }

    private void persist(final Object entity) {
        em.persist(entity);
        em.flush();
    }

    public Object getIdFor(final Object expectedEntity) {
        return em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(expectedEntity);
    }

    public Object findActualEntityFor(final Object expectedEntity) {
        final Object id = getIdFor(expectedEntity);
        final Class<?> entityClass = expectedEntity.getClass();
        LOGGER.debug("Looking up entity of class {} with ID {}", entityClass, id);

        final Object result = em.find(entityClass, id);
        LOGGER.debug("Found: {}", result);

        return result;
    }

    public void withTransaction(final ExecuteInTransaction executeInTransaction) {
        final EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            executeInTransaction.execute();
        } catch (final RuntimeException e) {
            tx.rollback();
            throw e;
        }

        tx.commit();
    }

    public void close() {
        em.close();
    }

    public interface ExecuteInTransaction {
        void execute();
    }

}
