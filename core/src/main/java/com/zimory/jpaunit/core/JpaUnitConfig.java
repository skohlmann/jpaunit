package com.zimory.jpaunit.core;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.zimory.jpaunit.core.annotation.GenerateExpectedDataSet;
import com.zimory.jpaunit.core.annotation.GenerateSetupDataSet;
import com.zimory.jpaunit.core.serialization.YamlScalarSerializer;

public class JpaUnitConfig {

    public static final String DEFAULT_DATASET_DIR = "/datasets";

    private String datasetDir = DEFAULT_DATASET_DIR;
    private List<? extends YamlScalarSerializer<?>> serializers = ImmutableList.of();
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

    public List<? extends YamlScalarSerializer<?>> getSerializers() {
        return serializers;
    }

    public void setSerializers(final List<? extends YamlScalarSerializer<?>> serializers) {
        this.serializers = ImmutableList.copyOf(serializers);
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
