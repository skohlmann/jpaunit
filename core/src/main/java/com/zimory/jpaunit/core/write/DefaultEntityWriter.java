package com.zimory.jpaunit.core.write;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.EntityType;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlWriter;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableSortedSet;
import com.zimory.jpaunit.core.context.JpaUnitConfig;
import com.zimory.jpaunit.core.util.PathUtil;
import com.zimory.jpaunit.core.context.TestContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
public class DefaultEntityWriter implements EntityWriter {

    private final JpaUnitConfig config;
    private final YamlConfig yamlConfig;
    private final EntityManager em;

    @Inject
    public DefaultEntityWriter(final JpaUnitConfig config, final YamlConfig yamlConfig, final EntityManager em) {
        this.config = config;
        this.yamlConfig = yamlConfig;
        this.em = em;
    }

    @Override
    public void writeSetupEntities(final TestContext testContext) {
        final ImmutableList<Method> methods = testContext.getSetupMethods();

        for (final Method m : methods) {
            final Optional<String> relativePath = PathUtil.getGenerateSetupDataSetRelativePath(m, config);
            if (!relativePath.isPresent()) {
                continue;
            }

            final String path = PathUtil.formatYamlPath(config.getWriterBaseDir(), relativePath.get());

            writeEntities(path);
        }
    }

    @Override
    public void writeExpectEntities(final TestContext testContext) {
        final ImmutableList<Method> methods = testContext.getExpectMethods();

        for (final Method m : methods) {
            final Annotation a = m.getAnnotation(config.getGenerateExpectedDataSetAnnotation());

            if (a != null) {
                final Optional<String> relativePath = PathUtil.getGenerateExpectedDataSetRelativePath(m, config);
                if (!relativePath.isPresent()) {
                    continue;
                }

                final String path = PathUtil.formatYamlPath(config.getWriterBaseDir(), relativePath.get());

                writeEntities(path);
            }
        }
    }

    private void writeEntities(final String path) {
        final YamlWriter writer;

        try {
            final File file = new File(path);
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                throw new IllegalStateException("Could not create directory: " + file.getParent());
            }

            writer = new YamlWriter(new FileWriter(file), yamlConfig);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        try {
            try {
                for (final Object entity : findAll()) {
                    writer.write(entity);
                }
            } catch (final YamlException e) {
                throw new RuntimeException(e);
            }
        } finally {
            try {
                writer.close();
            } catch (final YamlException e) {
                e.printStackTrace();
            }
        }
    }

    private List<Object> findAll() {
        final Builder<Object> builder = ImmutableList.builder();

        for (final EntityType<?> e : getEntityTypes()) {
            builder.addAll(findAll(e.getJavaType()));
        }

        return builder.build();
    }

    private <T> List<T> findAll(final Class<T> javaType) {
        final CriteriaBuilder cb = em.getCriteriaBuilder();
        final CriteriaQuery<T> cq = cb.createQuery(javaType);

        cq.from(javaType);

        return em.createQuery(cq).getResultList();
    }

    private Set<EntityType<?>> getEntityTypes() {
        final Set<EntityType<?>> unsortedEntityTypes = em.getEntityManagerFactory().getMetamodel().getEntities();

        if (config.getEntityTypeOrdering() == null) {
            return unsortedEntityTypes;
        }

        return ImmutableSortedSet.copyOf(new Comparator<EntityType<?>>() {
            @Override
            public int compare(final EntityType<?> o1, final EntityType<?> o2) {
                return config.getEntityTypeOrdering().compare(o1.getJavaType(), o2.getJavaType());
            }
        }, unsortedEntityTypes);
    }

}
