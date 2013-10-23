package com.zimory.jpaunit.core.read;

import java.io.IOException;
import java.util.Set;

import com.zimory.jpaunit.core.context.TestContext;

public interface EntityReader {

    Set<Object> readSetupEntities(TestContext testContext) throws IOException;

    Set<Object> readExpectEntities(TestContext testContext) throws IOException;

}
