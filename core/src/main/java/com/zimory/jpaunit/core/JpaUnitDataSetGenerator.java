package com.zimory.jpaunit.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlWriter;
import com.zimory.jpaunit.core.Dao.WithTransaction;

public class JpaUnitDataSetGenerator {

    private final Context context;

    public JpaUnitDataSetGenerator(final JpaUnitConfig config, final EntityManagerFactory entityManagerFactory) {
        this(new Context(config, entityManagerFactory));
    }

    JpaUnitDataSetGenerator(final Context context) {
        this.context = context;
    }

    public void before(final Method m, final Annotation a) {
        write(PathUtil.formatYamlPath(context.getConfig().getWriterBaseDir(),
                PathUtil.getGenerateSetupDataSetRelativePath(m, a)));
    }

    public void after(final Method m, final Annotation a) {
        write(PathUtil.formatYamlPath(context.getConfig().getWriterBaseDir(),
                PathUtil.getGenerateExpectedDataSetRelativePath(m, a)));
    }

    private void write(final String path) {
        final YamlWriter writer;

        try {
            final YamlConfig yamlConfig = context.newYamlConfig();
            final File file = new File(path);
            if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                throw new IllegalStateException("Could not create directory: " + file.getParent());
            }

            writer = new YamlWriter(new FileWriter(file), yamlConfig);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        try {
            final Dao dao = context.dao();

            dao.withTransaction(new WithTransaction() {
                @Override
                public void execute(final EntityManager em, final EntityTransaction tx) {
                    try {
                        for (final Object entity : dao.findAll()) {
                            writer.write(entity);
                        }
                    } catch (final YamlException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } finally {
            try {
                writer.close();
            } catch (final YamlException e) {
                e.printStackTrace();
            }
        }
    }

}
