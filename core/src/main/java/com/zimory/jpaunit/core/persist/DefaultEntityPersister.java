package com.zimory.jpaunit.core.persist;

import java.util.Collection;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class DefaultEntityPersister implements EntityPersister {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultEntityPersister.class);

    private final EntityManager em;

    @Inject
    public DefaultEntityPersister(final EntityManager em) {
        this.em = em;
    }

    @Override
    public void persist(final Collection<Object> entities) {
        for (final Object entity : entities) {
            LOGGER.debug("Persisting entity: {}", entity);

            em.persist(entity);
            em.flush();
        }
    }

}
