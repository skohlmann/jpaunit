package com.zimory.jpaunit.core.write;

import com.zimory.jpaunit.core.context.TestContext;

public interface EntityWriter {

    void writeSetupEntities(TestContext testContext);

    void writeExpectEntities(TestContext testContext);

}
