package com.zimory.jpaunit.core.junit;

import java.io.IOException;

import javax.persistence.EntityManagerFactory;

import com.google.common.base.Supplier;
import com.zimory.jpaunit.core.JpaUnit;
import com.zimory.jpaunit.core.context.JpaUnitConfig;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JpaUnitRule extends TestWatcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaUnitRule.class);

    private final Supplier<JpaUnitConfig> config;
    private final Supplier<EntityManagerFactory> entityManagerFactory;

    public JpaUnitRule(final Supplier<JpaUnitConfig> config, final Supplier<EntityManagerFactory> entityManagerFactory) {
        this.config = config;
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    protected void starting(final Description description) {
        try {
            newJpaUnit().setup(description.getTestClass(), description.getMethodName());
        } catch (final IOException e) {
            LOGGER.error("Caught IOException while setting up the test", e);
        }
    }

    @Override
    protected void succeeded(final Description description) {
        try {
            newJpaUnit().expect(description.getTestClass(), description.getMethodName());
        } catch (final IOException e) {
            LOGGER.error("Caught IOException while running expectations for the test", e);
        }
    }

    private JpaUnit newJpaUnit() {
        return JpaUnit.newInstance(config.get(), entityManagerFactory.get());
    }

}
