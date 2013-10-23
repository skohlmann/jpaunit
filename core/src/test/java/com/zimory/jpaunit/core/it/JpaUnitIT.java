package com.zimory.jpaunit.core.it;

import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.zimory.jpaunit.core.context.JpaUnitConfig;
import com.zimory.jpaunit.core.junit.JpaUnitRule;
import com.zimory.jpaunit.core.annotation.ShouldMatchJpaDataSet;
import com.zimory.jpaunit.core.annotation.UsingJpaDataSet;
import com.zimory.jpaunit.core.it.model.Unit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class JpaUnitIT {

    private static EntityManagerFactory emf;

    @BeforeClass
    public static void setUpClass() {
        emf = Persistence.createEntityManagerFactory("test");
    }

    @AfterClass
    public static void tearDownClass() {
        emf.close();
    }

    public static final class InnerTest {

        @Rule
        public JpaUnitRule jpaUnitRule = new JpaUnitRule(
                Suppliers.ofInstance(new JpaUnitConfig()), new Supplier<EntityManagerFactory>() {
            @Override
            public EntityManagerFactory get() {
                return emf;
            }
        });

        private EntityManager em;

        @Before
        public void setUp() {
            em = emf.createEntityManager();
        }

        @After
        public void tearDown() {
            em.close();
        }

        @Test
        @UsingJpaDataSet
        @ShouldMatchJpaDataSet
        public void findAndPersist() {
            em.getTransaction().begin();

            final Unit u0 = em.find(Unit.class, new UUID(0, 0));

            assertThat(u0, notNullValue());
            assertThat(u0.getName(), equalTo("old MacDonald had a farm"));

            final Unit u1 = new Unit();
            u1.setId(new UUID(0, 1));
            u1.setName("E-I-E-I-O");

            em.persist(u1);
            em.getTransaction().commit();
        }

    }

    @Test
    public void pretendTest() throws Exception {
        final Result result = JUnitCore.runClasses(InnerTest.class);

        if (!result.getFailures().isEmpty()) {
            for (final Failure failure : result.getFailures()) {
                failure.getException().printStackTrace();
            }
        }

        assertThat(result.wasSuccessful(), equalTo(true));
    }

}
