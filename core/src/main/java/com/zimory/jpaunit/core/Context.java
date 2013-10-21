package com.zimory.jpaunit.core;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.metamodel.EntityType;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.zimory.jpaunit.core.serialization.YamlScalarSerializer;

class Context {

    private final JpaUnitConfig config;
    private final EntityManagerFactory entityManagerFactory;

    Context(final JpaUnitConfig config, final EntityManagerFactory entityManagerFactory) {
        this.config = config;
        this.entityManagerFactory = entityManagerFactory;
    }

    YamlConfig newYamlConfig() {
        final YamlConfig yamlConfig = new YamlConfig();
        yamlConfig.writeConfig.setIndentSize(2);
        yamlConfig.setPrivateFields(true);

        for (final YamlScalarSerializer serializer : config.getSerializers()) {
            yamlConfig.setScalarSerializer(serializer.getType(), Serializers.scalarSerializerOf(serializer));
        }

        for (final EntityType<?> e : entityManagerFactory.getMetamodel().getEntities()) {
            yamlConfig.setClassTag(e.getName(), e.getJavaType());
        }

        return yamlConfig;
    }

    JpaUnitConfig getConfig() {
        return config;
    }

    Dao dao() {
        return new Dao(newEntityManager(), config.getEntityTypeOrdering());
    }

    private EntityManager newEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

}
