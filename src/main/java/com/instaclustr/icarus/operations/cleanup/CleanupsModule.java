package com.instaclustr.icarus.operations.cleanup;


import com.google.inject.AbstractModule;

import static com.instaclustr.operations.OperationBindings.installOperationBindings;

public class CleanupsModule extends AbstractModule {

    @Override
    protected void configure() {
        installOperationBindings(binder(),
                                 "cleanup",
                                 CleanupOperationRequest.class,
                                 CleanupOperation.class);
    }
}
