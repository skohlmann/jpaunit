package com.zimory.jpaunit.spring;

import java.util.List;

import com.google.common.collect.ImmutableList;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

public class TransactionalJpaUnitTestExecutionListener implements TestExecutionListener {

    private static final ImmutableList<? extends TestExecutionListener> CHAIN =
            ImmutableList.of(new JpaUnitTestExecutionListener(), new TransactionalTestExecutionListener());

    @Override
    public void beforeTestClass(final TestContext testContext) throws Exception {
        run(CHAIN, new Call() {
            @Override
            public void call(final TestExecutionListener listener) throws Exception {
                listener.beforeTestClass(testContext);
            }
        });
    }

    @Override
    public void prepareTestInstance(final TestContext testContext) throws Exception {
        run(CHAIN, new Call() {
            @Override
            public void call(final TestExecutionListener listener) throws Exception {
                listener.prepareTestInstance(testContext);
            }
        });
    }

    @Override
    public void beforeTestMethod(final TestContext testContext) throws Exception {
        run(CHAIN, new Call() {
            @Override
            public void call(final TestExecutionListener listener) throws Exception {
                listener.beforeTestMethod(testContext);
            }
        });
    }

    @Override
    public void afterTestMethod(final TestContext testContext) throws Exception {
        run(CHAIN.reverse(), new Call() {
            @Override
            public void call(final TestExecutionListener listener) throws Exception {
                listener.afterTestMethod(testContext);
            }
        });
    }

    @Override
    public void afterTestClass(final TestContext testContext) throws Exception {
        run(CHAIN.reverse(), new Call() {
            @Override
            public void call(final TestExecutionListener listener) throws Exception {
                listener.afterTestClass(testContext);
            }
        });
    }

    private void run(final List<? extends TestExecutionListener> chain, final Call call) throws Exception {
        for (final TestExecutionListener listener : chain) {
            call.call(listener);
        }
    }

    private interface Call {
        void call(TestExecutionListener listener) throws Exception;
    }

}
