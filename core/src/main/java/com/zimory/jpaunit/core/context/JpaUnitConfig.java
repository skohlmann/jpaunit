package com.zimory.jpaunit.core.context;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.zimory.jpaunit.core.annotation.GenerateExpectedDataSet;
import com.zimory.jpaunit.core.annotation.GenerateSetupDataSet;
import com.zimory.jpaunit.core.serialization.UuidSerializer;
import com.zimory.jpaunit.core.serialization.YamlScalarSerializer;

public class JpaUnitConfig {

    public static final String DEFAULT_DATASET_DIR = "/datasets";
    public static final List<? extends YamlScalarSerializer<?>> DEFAULT_SERIALIZERS = ImmutableList.of(new UuidSerializer());

    private String datasetDir = DEFAULT_DATASET_DIR;

    private boolean useDefaultSerializers = true;
    private List<YamlScalarSerializer<?>> customSerializers = ImmutableList.of();

    private Comparator<Class<?>> entityTypeOrdering;
    private String writerBaseDir = System.getProperty("user.dir");

    private Class<? extends Annotation> generateSetupDataSetAnnotation = GenerateSetupDataSet.class;
    private Class<? extends Annotation> generateExpectedDataSetAnnotation = GenerateExpectedDataSet.class;

    public String getDatasetDir() {
        return datasetDir;
    }

    public void setDatasetDir(final String datasetDir) {
        this.datasetDir = datasetDir;
    }

    public List<YamlScalarSerializer<?>> getAllSerializers() {
        final Builder<YamlScalarSerializer<?>> builder = ImmutableList.builder();

        if (useDefaultSerializers) {
            builder.addAll(DEFAULT_SERIALIZERS);
        }

        builder.addAll(customSerializers);

        return builder.build();
    }

    public boolean isUseDefaultSerializers() {
        return useDefaultSerializers;
    }

    public void setUseDefaultSerializers(final boolean useDefaultSerializers) {
        this.useDefaultSerializers = useDefaultSerializers;
    }

    public List<YamlScalarSerializer<?>> getCustomSerializers() {
        return customSerializers;
    }

    public void setCustomSerializers(final List<YamlScalarSerializer<?>> customSerializers) {
        this.customSerializers = ImmutableList.copyOf(customSerializers);
    }

    public Comparator<Class<?>> getEntityTypeOrdering() {
        return entityTypeOrdering;
    }

    public void setEntityTypeOrdering(final Comparator<Class<?>> entityTypeOrdering) {
        this.entityTypeOrdering = entityTypeOrdering;
    }

    public void setWriterBaseDir(String writerBaseDir) {
        this.writerBaseDir = writerBaseDir;
    }

    public String getWriterBaseDir() {
        return writerBaseDir;
    }

    public Class<? extends Annotation> getGenerateSetupDataSetAnnotation() {
        return generateSetupDataSetAnnotation;
    }

    public void setGenerateSetupDataSetAnnotation(final Class<? extends Annotation> generateSetupDataSetAnnotation) {
        this.generateSetupDataSetAnnotation = generateSetupDataSetAnnotation;
    }

    public Class<? extends Annotation> getGenerateExpectedDataSetAnnotation() {
        return generateExpectedDataSetAnnotation;
    }

    public void setGenerateExpectedDataSetAnnotation(final Class<? extends Annotation> generateExpectedDataSetAnnotation) {
        this.generateExpectedDataSetAnnotation = generateExpectedDataSetAnnotation;
    }

}
