package com.zimory.jpaunit.spring;

import javax.persistence.EntityManagerFactory;

import com.google.common.base.Preconditions;
import com.zimory.jpaunit.core.JpaUnit;
import com.zimory.jpaunit.core.JpaUnitConfig;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class JpaUnitTestExecutionListener extends AbstractTestExecutionListener {

    @Override
    public void beforeTestMethod(final TestContext testContext) throws Exception {
        newJpaUnit(testContext).setup(testContext.getTestClass(), testContext.getTestMethod().getName());
    }

    @Override
    public void afterTestMethod(final TestContext testContext) throws Exception {
        if (testContext.getTestException() == null) {
            newJpaUnit(testContext).expect(testContext.getTestClass(), testContext.getTestMethod().getName());
        }
    }

    private static JpaUnit newJpaUnit(final TestContext testContext) {
        return new JpaUnit(getConfig(testContext), getEntityManagerFactory(testContext));
    }

    private static JpaUnitConfig getConfig(final TestContext testContext) {
        final String[] beanNames = testContext.getApplicationContext().getBeanNamesForType(JpaUnitConfig.class);
        Preconditions.checkState(beanNames.length <= 1, "can only be one instance of JpaUnitConfig");

        return beanNames.length == 0 ? new JpaUnitConfig() :
                testContext.getApplicationContext().getBean(beanNames[0], JpaUnitConfig.class);
    }

    private static EntityManagerFactory getEntityManagerFactory(final TestContext testContext) {
        return testContext.getApplicationContext().getBean(EntityManagerFactory.class);
    }

}
