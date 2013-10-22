package com.zimory.jpaunit.core;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.zimory.jpaunit.core.annotation.GenerateExpectedDataSet;
import com.zimory.jpaunit.core.annotation.GenerateSetupDataSet;
import com.zimory.jpaunit.core.annotation.ShouldMatchJpaDataSet;
import com.zimory.jpaunit.core.annotation.UsingJpaDataSet;

final class PathUtil {

    private static final String EXPECTED_PREFIX = "expected-";

    private static final String YAML_EXTENSION = "yml";

    private PathUtil() {
        throw new UnsupportedOperationException("Non-instantiable");
    }

    public static String[] getRelativePaths(final Method m, final UsingJpaDataSet a) {
        if (a.value().length > 0) {
            return a.value();
        }

        return new String[] {getSetupDataSetPath(m)};
    }

    public static String[] getRelativePaths(final Method m, final ShouldMatchJpaDataSet a) {
        if (a.value().length > 0) {
            return a.value();
        }

        return new String[] {getExpectedDataSetPath(m)};
    }

    public static String getGenerateSetupDataSetRelativePath(final Method m, final Annotation a) {
        if (a instanceof GenerateSetupDataSet) {
            final GenerateSetupDataSet generateSetupDataSet = (GenerateSetupDataSet) a;

            if (!Strings.isNullOrEmpty(generateSetupDataSet.value())) {
                return generateSetupDataSet.value();
            }
        }

        return getSetupDataSetPath(m);
    }

    public static String getGenerateExpectedDataSetRelativePath(final Method m, final Annotation a) {
        if (a instanceof GenerateExpectedDataSet) {
            final GenerateExpectedDataSet generateSetupDataSet = (GenerateExpectedDataSet) a;

            if (!Strings.isNullOrEmpty(generateSetupDataSet.value())) {
                return generateSetupDataSet.value();
            }
        }

        return getExpectedDataSetPath(m);
    }

    public static String getSetupDataSetPath(final Method m) {
        return m.getDeclaringClass().getSimpleName() + "/" + m.getName();
    }

    public static String getExpectedDataSetPath(final Method m) {
        return m.getDeclaringClass().getSimpleName() + "/" + EXPECTED_PREFIX + m.getName();
    }

    public static String formatYamlPath(final String baseDir, final String relativePath) {
        final String extension = YAML_EXTENSION.equals(Files.getFileExtension(relativePath)) ? "" : "." + YAML_EXTENSION;

        return String.format("%s/%s%s", baseDir, relativePath, extension);
    }

}
