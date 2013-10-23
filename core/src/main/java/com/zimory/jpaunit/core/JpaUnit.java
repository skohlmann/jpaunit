package com.zimory.jpaunit.core;

import java.io.IOException;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;

import com.zimory.jpaunit.core.compare.EntityComparator;
import com.zimory.jpaunit.core.context.Beans;
import com.zimory.jpaunit.core.context.JpaUnitConfig;
import com.zimory.jpaunit.core.context.TestContext;
import com.zimory.jpaunit.core.persist.EntityPersister;
import com.zimory.jpaunit.core.read.EntityReader;
import com.zimory.jpaunit.core.write.EntityWriter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

@Component
public final class JpaUnit {

    private final EntityReader entityReader;
    private final EntityWriter entityWriter;
    private final EntityPersister entityPersister;
    private final EntityComparator entityComparator;

    @Inject
    private JpaUnit(
            final EntityReader entityReader,
            final EntityWriter entityWriter,
            final EntityPersister entityPersister,
            final EntityComparator entityComparator) {
        this.entityReader = entityReader;
        this.entityWriter = entityWriter;
        this.entityPersister = entityPersister;
        this.entityComparator = entityComparator;
    }

    public void setup(final Class<?> testClass, final String testMethodName) throws IOException {
        final TestContext testContext = new TestContext(testClass, testMethodName);
        final Set<Object> entities = entityReader.readSetupEntities(testContext);

        entityWriter.writeSetupEntities(testContext);

        if (entities.isEmpty()) {
            return;
        }

        entityPersister.persist(entities);
    }

    public void expect(final Class<?> testClass, final String testMethodName) throws IOException {
        final TestContext testContext = new TestContext(testClass, testMethodName);
        final Set<Object> expectEntities = entityReader.readExpectEntities(testContext);
        final Set<Object> setupEntities = entityReader.readSetupEntities(testContext);

        entityWriter.writeExpectEntities(testContext);

        if (expectEntities.isEmpty()) {
            return;
        }

        entityComparator.compare(expectEntities, setupEntities);
    }

    public static JpaUnit newInstance(final JpaUnitConfig config, final EntityManagerFactory entityManagerFactory) {
        final ConfigurableApplicationContext context = createContext(config, entityManagerFactory);

        return context.getBean(JpaUnit.class);
    }

    private static ConfigurableApplicationContext createContext(final JpaUnitConfig cfg, final EntityManagerFactory emf) {
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.addBeanFactoryPostProcessor(new BeanFactoryPostProcessor() {
            @Override
            public void postProcessBeanFactory(final ConfigurableListableBeanFactory beanFactory) throws BeansException {
                beanFactory.registerSingleton("jpaUnitConfig", cfg);
                beanFactory.registerSingleton("entityManagerFactory", emf);
            }
        });

        context.register(Beans.class);
        context.refresh();

        return context;
    }

}
