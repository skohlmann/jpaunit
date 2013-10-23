package com.zimory.jpaunit.core.context;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnitUtil;
import javax.persistence.metamodel.EntityType;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.zimory.jpaunit.core.serialization.YamlScalarSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@ComponentScan("com.zimory.jpaunit.core")
@EnableTransactionManagement
public class Beans {

    @Bean
    public YamlConfig yamlConfig(final EntityManagerFactory emf, final JpaUnitConfig config) {
        final YamlConfig yamlConfig = new YamlConfig();
        yamlConfig.writeConfig.setIndentSize(2);
        yamlConfig.setPrivateFields(true);

        for (final YamlScalarSerializer serializer : config.getAllSerializers()) {
            yamlConfig.setScalarSerializer(serializer.getType(), Serializers.scalarSerializerOf(serializer));
        }

        for (final EntityType<?> e : emf.getMetamodel().getEntities()) {
            yamlConfig.setClassTag(e.getName(), e.getJavaType());
        }

        return yamlConfig;
    }

    @Bean
    public PlatformTransactionManager transactionManager(final EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public PersistenceUnitUtil persistenceUnitUtil(final EntityManagerFactory entityManagerFactory) {
        return entityManagerFactory.getPersistenceUnitUtil();
    }

}
