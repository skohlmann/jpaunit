package com.zimory.jpaunit.core;

import java.lang.reflect.Method;
import java.util.Arrays;

import com.google.common.collect.ImmutableList;
import com.zimory.jpaunit.core.context.TestContext;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertThat;

public class TestContextTest {

    public static abstract class SuperClass {
        @Before
        public void before0() {}
        @After
        public void after0() {}
    }

    public static final class SubClass extends SuperClass {
        @Before
        public void before1() {}
        @Before
        public void before2() {}
        @Test
        public void test() {}
        @After
        public void after1() {}
        @After
        public void after2() {}
    }

    private TestContext testContext;

    @Before
    public void setUp() {
        final Class<?> cls = SubClass.class;
        testContext = new TestContext(cls, "test");
    }

    @Test
    public void getSetupMethods() throws Exception {
        final ImmutableList<Method> methods = testContext.getSetupMethods();

        assertThat(methods.get(0), methodNameEqualToOneOf("before0"));
        assertThat(methods.get(1), methodNameEqualToOneOf("before1", "before2"));
        assertThat(methods.get(2), methodNameEqualToOneOf("before1", "before2"));
        assertThat(methods.get(3), methodNameEqualToOneOf("test"));
    }

    @Test
    public void getExpectMethods() throws Exception {
        final ImmutableList<Method> methods = testContext.getExpectMethods();

        assertThat(methods.get(0), methodNameEqualToOneOf("after0"));
        assertThat(methods.get(1), methodNameEqualToOneOf("after1", "after2"));
        assertThat(methods.get(2), methodNameEqualToOneOf("after1", "after2"));
        assertThat(methods.get(3), methodNameEqualToOneOf("test"));
    }

    private static Matcher<? super Method> methodNameEqualToOneOf(final String... methodNames) {
        return new CustomTypeSafeMatcher<Method>("one of " + Arrays.toString(methodNames)) {

            @Override
            protected void describeMismatchSafely(final Method item, final Description mismatchDescription) {
                mismatchDescription.appendText("was " + item.getName());
            }

            @Override
            protected boolean matchesSafely(final Method item) {
                for (final String methodName : methodNames) {
                    if (methodName.equals(item.getName())) {
                        return true;
                    }
                }

                return false;
            }

        };
    }

}
