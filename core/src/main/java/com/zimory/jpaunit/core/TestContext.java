package com.zimory.jpaunit.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.After;
import org.junit.Before;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;
import org.unitils.util.ReflectionUtils;

class TestContext {

    private Class<?> testClass;
    private Method testMethod;

    public TestContext(final Class<?> testClass, final String testMethodName) {
        this.testClass = testClass;

        testMethod = getTestMethod(testClass, testMethodName);
    }

    private static Method getTestMethod(final Class<?> testClass, final String testMethodName) {
        return ReflectionUtils.getMethod(testClass, testMethodName, false);
    }

    public Class<?> getTestClass() {
        return testClass;
    }

    public Method getTestMethod() {
        return testMethod;
    }

    /**
     * Gets all methods to analyse when looking for "setup" methods (i.e. methods that might have a
     * {@link com.zimory.jpaunit.core.annotation.UsingJpaDataSet} annotation).
     *
     * @return all methods eligible for the setup phase
     */
    public ImmutableList<Method> getSetupMethods() {
        return ImmutableList.<Method>builder()
                .addAll(getAnnotatedMethods(getTestClass(), Before.class))
                .add(getTestMethod())
                .build();
    }

    /**
     * Gets all methods to analyse when looking for "expect" methods (i.e. methods that might have a
     * {@link com.zimory.jpaunit.core.annotation.ShouldMatchJpaDataSet} annotation).
     *
     * @return all methods eligible for the expect phase
     */
    public ImmutableList<Method> getExpectMethods() {
        // JUnit orders @Afters starting from subclass to superclass, but we need it in reverse
        return ImmutableList.<Method>builder()
                .addAll(getAnnotatedMethods(getTestClass(), After.class).reverse())
                .add(getTestMethod())
                .build();
    }

    private static ImmutableList<Method> getAnnotatedMethods(
            final Class<?> testClass, final Class<? extends Annotation> a) {
        final List<Method> transformed = Lists.transform(new TestClass(testClass).getAnnotatedMethods(a),
                new Function<FrameworkMethod, Method>() {
                    @Override
                    public Method apply(final FrameworkMethod input) {
                        return input.getMethod();
                    }
                });

        return ImmutableList.copyOf(transformed);
    }

}
