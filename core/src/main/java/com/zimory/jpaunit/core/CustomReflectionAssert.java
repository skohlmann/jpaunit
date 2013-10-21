package com.zimory.jpaunit.core;

import java.util.List;

import com.google.common.collect.ImmutableList;
import org.unitils.reflectionassert.ReflectionComparator;
import org.unitils.reflectionassert.comparator.Comparator;
import org.unitils.reflectionassert.comparator.impl.CollectionComparator;
import org.unitils.reflectionassert.comparator.impl.HibernateProxyComparator;
import org.unitils.reflectionassert.comparator.impl.LenientNumberComparator;
import org.unitils.reflectionassert.comparator.impl.MapComparator;
import org.unitils.reflectionassert.comparator.impl.SimpleCasesComparator;
import org.unitils.reflectionassert.difference.Difference;
import org.unitils.reflectionassert.report.impl.DefaultDifferenceReport;

import static org.junit.Assert.fail;

class CustomReflectionAssert {

    // copied code from org.unitils.reflectionassert.ReflectionAssert and modified it to accommodate bi-directional
    // relationships
    static void assertReflectionEquals(final Object expected, final Object actual) {
        final List<Comparator> comparators = getComparators();
        final ReflectionComparator reflectionComparator = new ReflectionComparator(comparators);
        final Difference difference = reflectionComparator.getDifference(expected, actual);

        if (difference != null) {
            fail(getFailureMessage(difference));
        }
    }

    private static List<Comparator> getComparators() {
        return ImmutableList.<Comparator>builder()
                    .add(new LenientNumberComparator())
                    .add(new SimpleCasesComparator())
                    .add(new CollectionComparator())
                    .add(new MapComparator())
                    .add(new HibernateProxyComparator())
                    .add(new BiDirectionalRelationshipAwareObjectComparator())
                    .build();
    }

    private static String getFailureMessage(final Difference difference) {
        return new DefaultDifferenceReport().createReport(difference);
    }

}
