package com.zimory.jpaunit.core;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
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

    private final EntityManagerFactory emf;
    private final Comparator<Class<?>> entityTypeOrdering;

    public Dao(final EntityManagerFactory emf, final Comparator<Class<?>> entityTypeOrdering) {
        this.emf = emf;
        this.entityTypeOrdering = entityTypeOrdering;
    }

    public List<Object> findAll() {
        final Builder<Object> builder = ImmutableList.builder();

        withEntityManager(new WithEntityManager() {
            @Override
            public void execute(final EntityManager em) {
                for (final EntityType<?> e : getEntityTypes()) {
                    builder.addAll(findAll(em, e.getJavaType()));
                }
            }
        });

        return builder.build();
    }

    private static <T> List<T> findAll(final EntityManager em, final Class<T> javaType) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<T> cq = cb.createQuery(javaType);

        cq.from(javaType);

        return em.createQuery(cq).getResultList();
    }

    private Set<EntityType<?>> getEntityTypes() {
        final Set<EntityType<?>> unsortedEntityTypes = emf.getMetamodel().getEntities();

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

    public void persist(final Collection<Object> entities) {
        withTransaction(new WithTransaction() {
            @Override
            public void execute(final EntityManager em, final EntityTransaction tx) {
                for (final Object entity : entities) {
                    LOGGER.debug("Persisting entity: {}", entity);

                    em.persist(entity);
                    em.flush();
                }
            }
        });
    }

    private void withEntityManager(final WithEntityManager withEntityManager) {
        final EntityManager em = newEntityManager();

        try {
            withEntityManager.execute(em);
        } finally {
            em.close();
        }
    }

    public Object getIdFor(final Object expectedEntity) {
        return emf.getPersistenceUnitUtil().getIdentifier(expectedEntity);
    }

    public void withTransaction(final WithTransaction withTransaction) {
        withEntityManager(new WithEntityManager() {
            @Override
            public void execute(final EntityManager em) {
                final EntityTransaction tx = em.getTransaction();
                tx.begin();

                try {
                    withTransaction.execute(em, tx);
                } catch (final RuntimeException e) {
                    tx.rollback();
                    throw e;
                }

                tx.commit();
            }
        });
    }

    private EntityManager newEntityManager() {
        return emf.createEntityManager();
    }

    public interface WithTransaction {
        void execute(EntityManager em, EntityTransaction tx);
    }

    private interface WithEntityManager {
        void execute(EntityManager em);
    }

}
