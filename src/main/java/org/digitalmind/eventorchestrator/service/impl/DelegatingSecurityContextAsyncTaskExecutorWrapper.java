package org.digitalmind.eventorchestrator.service.impl;

import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor;

public class DelegatingSecurityContextAsyncTaskExecutorWrapper extends DelegatingSecurityContextAsyncTaskExecutor {

    public DelegatingSecurityContextAsyncTaskExecutorWrapper(AsyncTaskExecutor delegateAsyncTaskExecutor, SecurityContext securityContext) {
        super(delegateAsyncTaskExecutor, securityContext);
    }

    public DelegatingSecurityContextAsyncTaskExecutorWrapper(AsyncTaskExecutor delegateAsyncTaskExecutor) {
        super(delegateAsyncTaskExecutor);
    }

    public final AsyncTaskExecutor getDelegate() {
        return (AsyncTaskExecutor) getDelegateExecutor();
    }

}
