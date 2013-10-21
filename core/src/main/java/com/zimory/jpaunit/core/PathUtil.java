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

    static final String EXPECTED_PREFIX = "expected-";

    private PathUtil() {
        throw new UnsupportedOperationException("Non-instantiable");
    }

    static String[] getRelativePaths(final Method m, final UsingJpaDataSet a) {
        if (a.value().length > 0) {
            return a.value();
        }

        return new String[] {getSetupDataSetPath(m)};
    }

    static String[] getRelativePaths(final Method m, final ShouldMatchJpaDataSet a) {
        if (a.value().length > 0) {
            return a.value();
        }

        return new String[] {getExpectedDataSetPath(m)};
    }

    static String getGenerateSetupDataSetRelativePath(final Method m, final Annotation a) {
        if (a instanceof GenerateSetupDataSet) {
            final GenerateSetupDataSet generateSetupDataSet = (GenerateSetupDataSet) a;

            if (!Strings.isNullOrEmpty(generateSetupDataSet.value())) {
                return generateSetupDataSet.value();
            }
        }

        return getSetupDataSetPath(m);
    }

    static String getGenerateExpectedDataSetRelativePath(final Method m, final Annotation a) {
        if (a instanceof GenerateExpectedDataSet) {
            final GenerateExpectedDataSet generateSetupDataSet = (GenerateExpectedDataSet) a;

            if (!Strings.isNullOrEmpty(generateSetupDataSet.value())) {
                return generateSetupDataSet.value();
            }
        }

        return getExpectedDataSetPath(m);
    }

    static String getSetupDataSetPath(final Method m) {
        return m.getDeclaringClass().getSimpleName() + "/" + m.getName();
    }

    static String getExpectedDataSetPath(final Method m) {
        return m.getDeclaringClass().getSimpleName() + "/" + EXPECTED_PREFIX + m.getName();
    }

    static String formatYamlPath(final String baseDir, final String relativePath) {
        final String extension = "yml".equals(Files.getFileExtension(relativePath)) ? "" : ".yml";
        return String.format("%s/%s%s", baseDir, relativePath, extension);
    }

}
