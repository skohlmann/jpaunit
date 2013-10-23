package com.zimory.jpaunit.core.compare;

import java.util.Set;

public interface EntityComparator {

    void compare(Set<Object> expectedEntities, Set<Object> setupEntities);

}
