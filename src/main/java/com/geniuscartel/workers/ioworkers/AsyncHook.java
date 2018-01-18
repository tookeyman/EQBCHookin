package com.geniuscartel.workers.ioworkers;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

public interface AsyncHook {
    void acceptFuture(Callable future, ExecutorService IO_THREAD);
}
