package com.tcmyxc.zuul.cloud;

import com.tcmyxc.zuul.FilterLoader;
import com.tcmyxc.zuul.ZuulFilter;
import com.tcmyxc.zuul.filters.FilterRegistry;
import com.tcmyxc.zuul.monitoring.CounterFactory;
import com.tcmyxc.zuul.monitoring.TracerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.lang.reflect.Field;
import java.util.Map;

public class ZuulFilterInitializer {

    private static final Logger logger = LoggerFactory.getLogger(ZuulFilterInitializer.class);

    private final Map<String, ZuulFilter> filters;

    private final CounterFactory counterFactory;

    private final TracerFactory tracerFactory;

    private final FilterLoader filterLoader;

    private final FilterRegistry filterRegistry;

    public ZuulFilterInitializer(Map<String, ZuulFilter> filters, CounterFactory counterFactory,
                                 TracerFactory tracerFactory, FilterLoader filterLoader,
                                 FilterRegistry filterRegistry) {
        this.filters = filters;
        this.counterFactory = counterFactory;
        this.tracerFactory = tracerFactory;
        this.filterLoader = filterLoader;
        this.filterRegistry = filterRegistry;
    }

    @PostConstruct
    public void contextInitialized() {
        logger.info("Starting filter initializer");

        TracerFactory.initialize(tracerFactory);
        CounterFactory.init(counterFactory);

        // 注册filter
        for (Map.Entry<String, ZuulFilter> entry : filters.entrySet()) {
            filterRegistry.put(entry.getKey(), entry.getValue());
        }
    }

    @PreDestroy
    public void contextDestroyed() {
        logger.info("Stopping filter initializer");
        for (Map.Entry<String, ZuulFilter> entry : this.filters.entrySet()) {
            filterRegistry.remove(entry.getKey());
        }
        clearLoaderCache();

        TracerFactory.initialize(null);
        CounterFactory.init(null);
    }

    /**
     * 清空缓存
     */
    private void clearLoaderCache() {
        Field field = ReflectionUtils.findField(FilterLoader.class, "hashFiltersByType");
        ReflectionUtils.makeAccessible(field);
        @SuppressWarnings("rawtypes")
        Map cache = (Map) ReflectionUtils.getField(field, filterLoader);
        cache.clear();
    }
}
