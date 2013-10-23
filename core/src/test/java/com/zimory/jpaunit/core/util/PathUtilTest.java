package com.zimory.jpaunit.core.util;

import java.lang.reflect.Method;

import com.google.common.base.Optional;
import com.zimory.jpaunit.core.annotation.GenerateExpectedDataSet;
import com.zimory.jpaunit.core.annotation.GenerateSetupDataSet;
import com.zimory.jpaunit.core.annotation.ShouldMatchJpaDataSet;
import com.zimory.jpaunit.core.annotation.UsingJpaDataSet;
import com.zimory.jpaunit.core.context.JpaUnitConfig;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.unitils.util.ReflectionUtils.getMethod;

public class PathUtilTest {

    private static final String VALUE1 = "/somewhere";
    private static final String VALUE2 = "/over";
    private static final String VALUE3 = "/the";
    private static final String VALUE4 = "/rainbow";

    private static final String[] VALUES = {VALUE1, VALUE2, VALUE3, VALUE4};

    public static final class A {

        @UsingJpaDataSet(value = {VALUE1, VALUE2, VALUE3, VALUE4})
        @ShouldMatchJpaDataSet(value = {VALUE1, VALUE2, VALUE3, VALUE4})
        @GenerateSetupDataSet(value = VALUE1)
        @GenerateExpectedDataSet(value = VALUE1)
        public void withValues() {}

        @UsingJpaDataSet
        @ShouldMatchJpaDataSet
        @GenerateSetupDataSet
        @GenerateExpectedDataSet
        public void withoutValues() {}

        public void withoutAnnotations() {}

        public static Method getMethodWithAnnotationValues() {
            return getMethod(A.class, "withValues", false);
        }

        public static Method getMethodWithoutAnnotationValues() {
            return getMethod(A.class, "withoutValues", false);
        }

        public static Method getMethodWithoutAnnotations() {
            return getMethod(A.class, "withoutAnnotations", false);
        }

    }

    private static final JpaUnitConfig CONFIG = new JpaUnitConfig();

    private Method methodWithAnnotationValues;
    private Method methodWithoutAnnotationValues;
    private Method methodWithoutAnnotations;

    @Before
    public void setUp() throws Exception {
        methodWithAnnotationValues = A.getMethodWithAnnotationValues();
        methodWithoutAnnotationValues = A.getMethodWithoutAnnotationValues();
        methodWithoutAnnotations = A.getMethodWithoutAnnotations();
    }

    @Test
    public void GETS_SETUP_PATHS_whenValueIsNotEmptyThenReturnsValue() throws Exception {
        final String[] paths = PathUtil.GETS_SETUP_PATHS.apply(methodWithAnnotationValues);

        assertThat(paths, equalTo(VALUES));
    }

    @Test
    public void GETS_SETUP_PATHS_whenValueEmptyThenUsesMethodNameAndClassToConstructPath() throws Exception {
        final String[] paths = PathUtil.GETS_SETUP_PATHS.apply(methodWithoutAnnotationValues);

        assertThat(paths, equalTo(new String[] {"A/withoutValues"}));
    }

    @Test
    public void GETS_EXPECT_PATHS_whenValueIsNotEmptyThenReturnsValue() throws Exception {
        final String[] paths = PathUtil.GETS_EXPECT_PATHS.apply(methodWithAnnotationValues);

        assertThat(paths, equalTo(VALUES));
    }

    @Test
    public void GETS_EXPECT_PATHS_whenValueEmptyThenUsesMethodNameAndClassToConstructPath() throws Exception {
        final String[] paths = PathUtil.GETS_EXPECT_PATHS.apply(methodWithoutAnnotationValues);

        assertThat(paths, equalTo(new String[] {"A/expected-withoutValues"}));
    }

    @Test
    public void getGenerateSetupDataSetRelativePath_whenValueIsNotEmptyThenReturnsValue() throws Exception {
        final Optional<String> path = PathUtil.getGenerateSetupDataSetRelativePath(methodWithAnnotationValues, CONFIG);

        assertThat(path.isPresent(), equalTo(true));
        assertThat(path.get(), equalTo(VALUE1));
    }

    @Test
    public void getGenerateSetupDataSetRelativePath_whenValueEmptyThenUsesMethodNameAndClassToConstructPath() throws Exception {
        final Optional<String> path = PathUtil.getGenerateSetupDataSetRelativePath(methodWithoutAnnotationValues, CONFIG);

        assertThat(path.isPresent(), equalTo(true));
        assertThat(path.get(), equalTo("A/withoutValues"));
    }

    @Test
    public void getGenerateSetupDataSetRelativePath_whenNoAnnotationPresentReturnsAbsent() throws Exception {
        final Optional<String> path = PathUtil.getGenerateSetupDataSetRelativePath(methodWithoutAnnotations, CONFIG);

        assertThat(path.isPresent(), equalTo(false));
    }

    @Test
    public void getGenerateExpectedDataSetRelativePath_whenValueIsNotEmptyThenReturnsValue() throws Exception {
        final Optional<String> path = PathUtil.getGenerateExpectedDataSetRelativePath(methodWithAnnotationValues, CONFIG);

        assertThat(path.isPresent(), equalTo(true));
        assertThat(path.get(), equalTo(VALUE1));
    }

    @Test
    public void getGenerateExpectedDataSetRelativePath_whenValueEmptyThenUsesMethodNameAndClassToConstructPath() throws Exception {
        final Optional<String> path = PathUtil.getGenerateExpectedDataSetRelativePath(methodWithoutAnnotationValues, CONFIG);

        assertThat(path.isPresent(), equalTo(true));
        assertThat(path.get(), equalTo("A/expected-withoutValues"));
    }

    @Test
    public void getGenerateExpectedDataSetRelativePath_whenNoAnnotationPresentReturnsAbsent() throws Exception {
        final Optional<String> path = PathUtil.getGenerateExpectedDataSetRelativePath(methodWithoutAnnotations, CONFIG);

        assertThat(path.isPresent(), equalTo(false));
    }

    @Test
    public void formatYamlPath_whenPassedRelativePathWithoutExtensionAppendsYamlExtension() throws Exception {
        assertThat(PathUtil.formatYamlPath("basedir", "somedir/file"), equalTo("basedir/somedir/file.yml"));
    }

    @Test
    public void formatYamlPath_whenPassedRelativePathWithExtensionDoesntAppendAnything() throws Exception {
        assertThat(PathUtil.formatYamlPath("basedir", "somedir/file.yml"), equalTo("basedir/somedir/file.yml"));
    }

}
