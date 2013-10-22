package com.zimory.jpaunit.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterators;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import com.zimory.jpaunit.core.Dao.WithTransaction;
import com.zimory.jpaunit.core.annotation.ShouldMatchJpaDataSet;
import com.zimory.jpaunit.core.annotation.UsingJpaDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public final class JpaUnit {

    private static final Logger LOGGER = LoggerFactory.getLogger(JpaUnit.class);

    private static final int DEFAULT_BUFFER_SIZE = 8192;
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String YAML_ENTRY_SEPARATOR = LINE_SEPARATOR;

    private final Context context;

    public JpaUnit(final JpaUnitConfig config, final EntityManagerFactory entityManagerFactory) {
        this(new Context(config, entityManagerFactory));
    }

    private JpaUnit(final Context context) {
        this.context = context;
    }

    public void setup(final Class<?> testClass, final String testMethodName) throws IOException {
        final TestContext testContext = new TestContext(testClass, testMethodName);
        final Set<Object> entities = readSetupEntities(testContext);

        if (entities.isEmpty()) {
            return;
        }

        persist(entities);
    }

    private Set<Object> readSetupEntities(final TestContext testContext) throws IOException {
        final List<Method> methods = testContext.getSetupMethods();
        final String raw = readRaw(methods, UsingJpaDataSet.class);

        return readEntities(raw);
    }

    public void expect(final Class<?> testClass, final String testMethodName) throws IOException {
        final TestContext testContext = new TestContext(testClass, testMethodName);
        final String raw = readRaw(testContext.getExpectMethods(), ShouldMatchJpaDataSet.class);
        final Set<Object> entities = readEntities(raw);
        final Set<Object> setupEntities = readSetupEntities(testContext);

        if (entities.isEmpty()) {
            return;
        }

        compare(entities, setupEntities);
    }

    private void persist(final Set<Object> entities) {
        context.dao().persist(entities);
    }

    private void compare(final Set<Object> expectedEntities, final Set<Object> setupEntities) {
        final Dao dao = context.dao();

        dao.withTransaction(new WithTransaction() {
            @Override
            public void execute(final EntityManager em, final EntityTransaction tx) {
                final Set<EntityWrapper> wrappedExpectedEntities = EntityWrapper.wrap(dao, expectedEntities);
                final Set<EntityWrapper> wrappedSetupEntities = EntityWrapper.wrap(dao, setupEntities);

                compareExpectedEntities(wrappedExpectedEntities, dao, em);
                compareExpectedToBeRemovedEntities(wrappedSetupEntities, wrappedExpectedEntities, dao, em);
            }
        });
    }

    private void compareExpectedEntities(
            final Set<EntityWrapper> expectedEntities,
            final Dao dao,
            final EntityManager em) {
        for (final EntityWrapper entityWrapper : expectedEntities) {
            final Object expectedEntity = entityWrapper.getEntity();

            final Object id = dao.getIdFor(expectedEntity);
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
            final Set<EntityWrapper> expectedEntities,
            final Dao dao,
            final EntityManager em) {
        final Set<EntityWrapper> expectedToBeRemoved = Sets.difference(setupEntities, expectedEntities);

        for (final EntityWrapper entityWrapper : expectedToBeRemoved) {
            final Object expectedEntity = entityWrapper.getEntity();

            final Object id = dao.getIdFor(expectedEntity);
            final Class<?> entityClass = expectedEntity.getClass();
            LOGGER.debug("Looking up entity of class {} with ID {}", entityClass, id);

            final Object actualEntity = em.find(entityClass, id);
            LOGGER.debug("Found: {}", actualEntity);

            LOGGER.debug("Expecting to be removed:\n  {}", expectedEntity);
            assertThat("Expected to be removed, but was present", actualEntity, nullValue());
        }
    }

    private String readRaw(final Collection<Method> methods, final Class<? extends Annotation> annotationCls) throws IOException {
        final StringBuilder buf = new StringBuilder(DEFAULT_BUFFER_SIZE);

        for (final Iterator<Method> methodIterator = methods.iterator(); methodIterator.hasNext();) {
            final Method m = methodIterator.next();
            maybeGenerateDataSets(m);

            final Annotation a = m.getAnnotation(annotationCls);
            if (a == null) {
                continue;
            }

            final String[] paths;

            if (a instanceof UsingJpaDataSet) {
                paths = PathUtil.getRelativePaths(m, (UsingJpaDataSet) a);
            } else if (a instanceof ShouldMatchJpaDataSet) {
                paths = PathUtil.getRelativePaths(m, (ShouldMatchJpaDataSet) a);
            } else {
                throw new IllegalStateException("Can't happen");
            }

            for (final Iterator<String> pathIterator = Iterators.forArray(paths); pathIterator.hasNext();) {
                final String yamlPath = PathUtil.formatYamlPath(context.getConfig().getDatasetDir(), pathIterator.next());
                final InputStream resource = getClass().getResourceAsStream(yamlPath);
                Preconditions.checkNotNull(resource, "resource not found: %s", yamlPath);

                CharStreams.copy(new InputStreamReader(resource), buf);

                if (pathIterator.hasNext()) {
                    buf.append(YAML_ENTRY_SEPARATOR);
                }
            }

            if (methodIterator.hasNext()) {
                buf.append(YAML_ENTRY_SEPARATOR);
            }
        }

        return buf.toString();
    }

    private void maybeGenerateDataSets(final Method m) {
        final Annotation generateSetupAnnotation = m.getAnnotation(
                context.getConfig().getGenerateSetupDataSetAnnotation());

        if (generateSetupAnnotation != null) {
            new JpaUnitDataSetGenerator(context).before(m, generateSetupAnnotation);
        }

        final Annotation generateExpectedAnnotation = m.getAnnotation(
                context.getConfig().getGenerateExpectedDataSetAnnotation());

        if (generateExpectedAnnotation != null) {
            new JpaUnitDataSetGenerator(context).after(m, generateExpectedAnnotation);
        }
    }

    private Set<Object> readEntities(final String raw) {
        final Set<Object> entities = Sets.newLinkedHashSet();
        final YamlReader reader = newYamlReader(new StringReader(raw));

        try {
            for (Object obj = reader.read(); obj != null; obj = reader.read()) {
                entities.add(obj);
            }
        } catch (final YamlException e) {
            LOGGER.debug("YAML raw source:\n{}", padSourceWithLineNumbers(raw));

            throw new RuntimeException(e);
        } finally {
            try {
                reader.close();
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }

        return entities;
    }

    private static String padSourceWithLineNumbers(final String raw) {
        final String[] lines = raw.split(LINE_SEPARATOR);
        final StringBuilder buf = new StringBuilder(raw.length() + lines.length);
        final int lineNumberPadding = (int) Math.log10(lines.length) + 1;
        final String lineFormat = "%" + lineNumberPadding + "d | %s%n";

        for (int i = 0; i < lines.length; i++) {
            final String line = lines[i];
            buf.append(String.format(lineFormat, i, line));
        }

        return buf.toString();
    }

    private YamlReader newYamlReader(final Reader r) {
        return new YamlReader(r, context.newYamlConfig());
    }

}
