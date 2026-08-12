package com.ristruct.shizuku.internal;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Threading {
    private static final ExecutorService IO = Executors.newCachedThreadPool();
    private Threading() {}
    public static void execute(Runnable r) { IO.execute(r); }
    public static void shutdown() { IO.shutdownNow(); }
}
