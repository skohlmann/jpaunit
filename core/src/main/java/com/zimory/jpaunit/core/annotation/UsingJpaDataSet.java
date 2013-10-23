package com.zimory.jpaunit.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the given test method is going to use the given dataset to pre-populate the database.
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UsingJpaDataSet {

    /**
     * Relative paths to datasets to use. If empty, the name will be constructed from the names of the class and method.
     *
     * @return relative paths to datasets
     */
    String[] value() default {};

}
