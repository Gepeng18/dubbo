/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.dubbo.monitor.support;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.concurrent.ListenableFuture;
import com.alibaba.dubbo.common.concurrent.ListenableFutureTask;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.monitor.Monitor;
import com.alibaba.dubbo.monitor.MonitorFactory;
import com.alibaba.dubbo.monitor.MonitorService;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * AbstractMonitorFactory. (SPI, Singleton, ThreadSafe)
 */
public abstract class AbstractMonitorFactory implements MonitorFactory {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMonitorFactory.class);

    // lock for getting monitor center
    private static final ReentrantLock LOCK = new ReentrantLock();

    // monitor centers Map<RegistryAddress, Registry>
    private static final Map<String, Monitor> MONITORS = new ConcurrentHashMap<String, Monitor>();

    private static final Map<String, ListenableFuture<Monitor>> FUTURES = new ConcurrentHashMap<String, ListenableFuture<Monitor>>();

    private static final ExecutorService executor = new ThreadPoolExecutor(0, 10, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(), new NamedThreadFactory("DubboMonitorCreator", true));

    public static Collection<Monitor> getMonitors() {
        return Collections.unmodifiableCollection(MONITORS.values());
    }

    @Override
    public Monitor getMonitor(URL url) {
        // 设置path com.alibaba.dubbo.monitor.MonitorService  interface =com.alibaba.dubbo.monitor.MonitorService
        url = url.setPath(MonitorService.class.getName()).addParameter(Constants.INTERFACE_KEY, MonitorService.class.getName());
        // 生成一个key
        String key = url.toServiceStringWithoutResolving();
        // 获取monitor
        Monitor monitor = MONITORS.get(key);
        Future<Monitor> future = FUTURES.get(key);
        // 如果缓存了具体的monitor就返回
        if (monitor != null || future != null) {
            return monitor;
        }

        LOCK.lock();
        try {
            monitor = MONITORS.get(key);
            future = FUTURES.get(key);
            if (monitor != null || future != null) {
                return monitor;
            }

            final URL monitorUrl = url;
            // 异步创建
            final ListenableFutureTask<Monitor> listenableFutureTask = ListenableFutureTask.create(new MonitorCreator(monitorUrl));
            // 注册一个listener，这个listener会在future执行完后将数monitor从future中取出来，放在MONITORS中
            listenableFutureTask.addListener(new MonitorListener(key));
            // 使用线程池执行创建逻辑，并且把这个任务放到futures中，以便后期能取出来
            executor.execute(listenableFutureTask);
            FUTURES.put(key, listenableFutureTask);

            return null;
        } finally {
            // unlock
            LOCK.unlock();
        }
    }

    protected abstract Monitor createMonitor(URL url);

    class MonitorCreator implements Callable<Monitor> {

        private URL url;

        public MonitorCreator(URL url) {
            this.url = url;
        }

        @Override
        public Monitor call() throws Exception {
            Monitor monitor = AbstractMonitorFactory.this.createMonitor(url);
            return monitor;
        }
    }

    /**
     * 这个MonitorListener会在Future执行完run方法的时候调用执行，也就是创建完成Monitor对象后执行。
     */
    class MonitorListener implements Runnable {

        private String key;

        public MonitorListener(String key) {
            this.key = key;
        }

        @Override
        public void run() {
            try {
                // 1、根据key从缓存中获取listenableFuture任务
                ListenableFuture<Monitor> listenableFuture = AbstractMonitorFactory.FUTURES.get(key);
                // 2、从ListenableFuture获取创建的那个Monitor对象，然后放到Monitor缓存中
                AbstractMonitorFactory.MONITORS.put(key, listenableFuture.get());
                ///3、从任务缓存中移除这个任务
                AbstractMonitorFactory.FUTURES.remove(key);
            } catch (InterruptedException e) {
                logger.warn("Thread was interrupted unexpectedly, monitor will never be got.");
                AbstractMonitorFactory.FUTURES.remove(key);
            } catch (ExecutionException e) {
                logger.warn("Create monitor failed, monitor data will not be collected until you fix this problem. ", e);
            }
        }
    }

}
