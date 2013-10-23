package com.zimory.jpaunit.core.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.zimory.jpaunit.core.annotation.ShouldMatchJpaDataSet;
import com.zimory.jpaunit.core.annotation.UsingJpaDataSet;
import com.zimory.jpaunit.core.context.JpaUnitConfig;
import org.unitils.util.ReflectionUtils;

public final class PathUtil {

    private static final String EXPECTED_PREFIX = "expected-";
    private static final String YAML_EXTENSION = "yml";

    private PathUtil() {
        throw new UnsupportedOperationException("Non-instantiable");
    }

    public static String[] getRelativeSetupPaths(final Method m) {
        final UsingJpaDataSet a = m.getAnnotation(UsingJpaDataSet.class);

        if (a.value().length > 0) {
            return a.value();
        }

        return new String[] {getSetupDataSetPath(m)};
    }

    public static String[] getRelativeExpectPaths(final Method m) {
        final ShouldMatchJpaDataSet a = m.getAnnotation(ShouldMatchJpaDataSet.class);

        if (a.value().length > 0) {
            return a.value();
        }

        return new String[] {getExpectedDataSetPath(m)};
    }

    public static Optional<String> getGenerateSetupDataSetRelativePath(final Method m, final JpaUnitConfig config) {
        final Annotation a = m.getAnnotation(config.getGenerateSetupDataSetAnnotation());
        if (a == null) {
            return Optional.absent();
        }

        final Optional<String> value = getValue(a);

        if (value.isPresent()) {
            return value;
        }

        return Optional.of(getSetupDataSetPath(m));
    }

    public static Optional<String> getGenerateExpectedDataSetRelativePath(final Method m, final JpaUnitConfig config) {
        final Annotation a = m.getAnnotation(config.getGenerateExpectedDataSetAnnotation());
        if (a == null) {
            return Optional.absent();
        }

        final Optional<String> value = getValue(a);

        if (value.isPresent()) {
            return value;
        }

        return Optional.of(getExpectedDataSetPath(m));
    }

    private static Optional<String> getValue(final Annotation a) {
        final Method valueMethod = ReflectionUtils.getMethod(a.annotationType(), "value", false);

        if (valueMethod == null || valueMethod.getReturnType() != String.class) {
            return Optional.absent();
        }

        try {
            final String value = ReflectionUtils.invokeMethod(a, valueMethod);
            if (Strings.isNullOrEmpty(value)) {
                return Optional.absent();
            }

            return Optional.of(value);
        } catch (final InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    private static String getSetupDataSetPath(final Method m) {
        return m.getDeclaringClass().getSimpleName() + "/" + m.getName();
    }

    private static String getExpectedDataSetPath(final Method m) {
        return m.getDeclaringClass().getSimpleName() + "/" + EXPECTED_PREFIX + m.getName();
    }

    public static String formatYamlPath(final String baseDir, final String relativePath) {
        final String extension = YAML_EXTENSION.equals(Files.getFileExtension(relativePath)) ? "" : "." + YAML_EXTENSION;

        return String.format("%s/%s%s", baseDir, relativePath, extension);
    }

}
