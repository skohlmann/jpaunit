package com.zimory.jpaunit.core.persist;

import java.util.Collection;

public interface EntityPersister {

    void persist(Collection<Object> entities);

}
