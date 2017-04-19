package org.nio.java._0test01;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 以单例设计模式创建线程池，减少创建线程所需的开销
 * Created by XJX on 2017/3/13.
 */
public class ExecutorServiceSingleton {

    private ExecutorServiceSingleton() {
    }

    private static class ExecutorServiceFactory {
        private static final ExecutorService INSTANCE = Executors.newFixedThreadPool(20);
    }

    public static final ExecutorService getInstance() {
        return ExecutorServiceFactory.INSTANCE;
    }
}
