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

import javax.persistence.EntityManagerFactory;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import com.zimory.jpaunit.core.Dao.ExecuteInTransaction;
import com.zimory.jpaunit.core.annotation.ShouldMatchJpaDataSet;
import com.zimory.jpaunit.core.annotation.UsingJpaDataSet;
import org.junit.After;
import org.junit.Before;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;
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

    public void setup(final Class<?> testClass, final Method testMethod) throws IOException {
        final Set<Object> entities = readSetupEntities(testClass, testMethod);

        if (entities.isEmpty()) {
            return;
        }

        persist(entities);
    }

    public void expect(final Class<?> testClass, final Method testMethod) throws IOException {
        final List<Method> methods = getEligibleMethods(testClass, testMethod, After.class);
        final String raw = readRaw(methods, ShouldMatchJpaDataSet.class);
        final Set<Object> entities = readEntities(raw);
        final Set<Object> setupEntities = readSetupEntities(testClass, testMethod);

        if (entities.isEmpty()) {
            return;
        }

        compare(entities, setupEntities);
    }

    private Set<Object> readSetupEntities(final Class<?> testClass, final Method testMethod) throws IOException {
        final List<Method> methods = getEligibleMethods(testClass, testMethod, Before.class);
        final String raw = readRaw(methods, UsingJpaDataSet.class);

        return readEntities(raw);
    }

    private static ImmutableList<Method> getEligibleMethods(
            final Class<?> testClass, final Method testMethod, final Class<? extends Annotation> a) {
        return ImmutableList.<Method>builder()
                .addAll(getAnnotatedMethods(testClass, a))
                .add(testMethod)
                .build();
    }

    private static List<Method> getAnnotatedMethods(final Class<?> testClass, final Class<? extends Annotation> a) {
        final List<Method> transformed = Lists.transform(new TestClass(testClass).getAnnotatedMethods(a),
                new Function<FrameworkMethod, Method>() {
                    @Override
                    public Method apply(final FrameworkMethod input) {
                        return input.getMethod();
                    }
                });

        final ImmutableList<Method> result = ImmutableList.copyOf(transformed);

        // JUnit orders @Afters starting from subclass to superclass, but we need it in reverse
        return a == After.class ? result.reverse() : result;
    }

    private void persist(final Set<Object> entities) {
        final Dao dao = context.dao();

        try {
            dao.persist(entities);
        } finally {
            dao.close();
        }
    }

    private void compare(final Set<Object> expectedEntities, final Set<Object> setupEntities) {
        final Dao dao = context.dao();

        try {
            dao.withTransaction(new ExecuteInTransaction() {
                @Override
                public void execute() {
                    compareExpectedEntities(expectedEntities, dao);
                    compareExpectedToBeRemovedEntities(setupEntities, expectedEntities, dao);
                }
            });
        } finally {
            dao.close();
        }
    }

    private void compareExpectedEntities(final Set<Object> expectedEntities, final Dao dao) {
        for (final Object expectedEntity : expectedEntities) {
            final Object actualEntity = dao.findActualEntityFor(expectedEntity);

            LOGGER.debug("Comparing:\n  expected: {}\n    actual: {}", expectedEntity, actualEntity);
            CustomReflectionAssert.assertReflectionEquals(expectedEntity, actualEntity);
        }
    }

    private void compareExpectedToBeRemovedEntities(
            final Set<Object> setupEntities, final Set<Object> expectedEntities, final Dao dao) {
        final Set<Object> expectedToBeRemoved = Sets.difference(setupEntities, expectedEntities);

        for (final Object expected : expectedToBeRemoved) {
            LOGGER.debug("Expecting to be removed:\n  {}", expected);
            assertThat("Expected to be removed, but was present", dao.findActualEntityFor(expected), nullValue());
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
