package com.zimory.jpaunit.core.compare;

import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnitUtil;

import com.google.common.collect.Sets;
import com.zimory.jpaunit.core.model.EntityWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

@Component
@Transactional
public class DefaultEntityComparator implements EntityComparator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEntityComparator.class);

    private final EntityManager em;

    @Inject
    public DefaultEntityComparator(final EntityManager em) {
        this.em = em;
    }

    @Override
    public void compare(final Set<Object> expectedEntities, final Set<Object> setupEntities) {
        final PersistenceUnitUtil util = em.getEntityManagerFactory().getPersistenceUnitUtil();
        final Set<EntityWrapper> wrappedExpectedEntities = EntityWrapper.wrap(util, expectedEntities);
        final Set<EntityWrapper> wrappedSetupEntities = EntityWrapper.wrap(util, setupEntities);

        compareExpectedEntities(wrappedExpectedEntities);
        compareExpectedToBeRemovedEntities(wrappedSetupEntities, wrappedExpectedEntities);
    }

    private void compareExpectedEntities(
            final Set<EntityWrapper> expectedEntities) {
        for (final EntityWrapper entityWrapper : expectedEntities) {
            final Object expectedEntity = entityWrapper.getEntity();

            final Object id = entityWrapper.getEntityId();
            final Class<?> entityClass = expectedEntity.getClass();
            LOGGER.debug("Looking up entity of class {} with ID {}", entityClass, id);

            final Object actualEntity = em.find(entityClass, id);
            LOGGER.debug("Found: {}", actualEntity);

            LOGGER.debug("Comparing:\n  expected: {}\n    actual: {}", expectedEntity, actualEntity);
            CustomReflectionAssert.assertReflectionEquals(expectedEntity, actualEntity);
        }
    }

    private void compareExpectedToBeRemovedEntities(
            final Set<EntityWrapper> setupEntities,
            final Set<EntityWrapper> expectedEntities) {
        final Set<EntityWrapper> expectedToBeRemoved = Sets.difference(setupEntities, expectedEntities);

        for (final EntityWrapper entityWrapper : expectedToBeRemoved) {
            final Object expectedEntity = entityWrapper.getEntity();

            final Object id = entityWrapper.getEntityId();
            final Class<?> entityClass = expectedEntity.getClass();
            LOGGER.debug("Looking up entity of class {} with ID {}", entityClass, id);

            final Object actualEntity = em.find(entityClass, id);
            LOGGER.debug("Found: {}", actualEntity);

            LOGGER.debug("Expecting to be removed:\n  {}", expectedEntity);
            assertThat("Expected to be removed, but was present", actualEntity, nullValue());
        }
    }

}
