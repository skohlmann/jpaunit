package com.zimory.jpaunit.core.compare;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.unitils.reflectionassert.ReflectionComparator;
import org.unitils.reflectionassert.comparator.impl.ObjectComparator;
import org.unitils.reflectionassert.difference.Difference;
import org.unitils.reflectionassert.difference.ObjectDifference;

import static java.lang.reflect.Modifier.isStatic;
import static java.lang.reflect.Modifier.isTransient;

class BiDirectionalRelationshipAwareObjectComparator extends ObjectComparator {

    @Override
    protected void compareFields(
            final Object left,
            final Object right,
            final Class<?> clazz,
            final ObjectDifference difference,
            final boolean onlyFirstDifference,
            final ReflectionComparator reflectionComparator) {
        final Field[] fields = clazz.getDeclaredFields();
        AccessibleObject.setAccessible(fields, true);

        for (final Field field : fields) {
            if (shouldSkip(field)) {
                continue;
            }

            try {
                // recursively check the value of the fields
                final Difference innerDifference = reflectionComparator
                        .getDifference(field.get(left), field.get(right), onlyFirstDifference);

                if (innerDifference != null) {
                    difference.addFieldDifference(field.getName(), innerDifference);
                    if (onlyFirstDifference) {
                        return;
                    }
                }
            } catch (final IllegalAccessException e) {
                // this can't happen. Would get a Security exception instead
                // throw a runtime exception in case the impossible happens.
                throw new InternalError("Unexpected IllegalAccessException");
            }
        }

        // compare fields declared in superclass
        Class<?> superclazz = clazz.getSuperclass();
        while (superclazz != null && !superclazz.getName().startsWith("java.lang")) {
            compareFields(left, right, superclazz, difference, onlyFirstDifference, reflectionComparator);
            superclazz = superclazz.getSuperclass();
        }
    }

    private static boolean shouldSkip(final Field field) {
        return isTransient(field.getModifiers())
                || isStatic(field.getModifiers())
                || field.isSynthetic()
                || isNotOwningSideOfRelationship(field);
    }

    private static boolean isNotOwningSideOfRelationship(final Field field) {
        return (field.getAnnotation(OneToOne.class) != null && !field.getAnnotation(OneToOne.class).mappedBy().isEmpty()) ||
                (field.getAnnotation(OneToMany.class) != null && !field.getAnnotation(OneToMany.class).mappedBy().isEmpty()) ||
                (field.getAnnotation(ManyToMany.class) != null && !field.getAnnotation(ManyToMany.class).mappedBy().isEmpty());
    }

}
