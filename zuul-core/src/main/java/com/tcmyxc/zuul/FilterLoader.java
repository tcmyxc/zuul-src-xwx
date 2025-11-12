package com.tcmyxc.zuul;


import com.tcmyxc.zuul.filters.FilterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * done
 */
public class FilterLoader {

    private static final Logger logger = LoggerFactory.getLogger(FilterLoader.class);

    // 饿汉式单例
    private static final FilterLoader INSTANCE = new FilterLoader();

    private FilterLoader(){}

    /**
     * 快表，根据filter type查询过滤器的时候，先从这个表里面查，如果没有，说明就是没有，直接返回；有的话再从filterRegistry里面查
     */
    private final ConcurrentHashMap<String, List<ZuulFilter>> hashFiltersByType = new ConcurrentHashMap<>();

    private FilterRegistry filterRegistry = FilterRegistry.instance();

    static FilterFactory FILTER_FACTORY = new DefaultFilterFactory();

    public void setFilterRegistry(FilterRegistry filterRegistry) {
        this.filterRegistry = filterRegistry;
    }

    public static void setFilterFactory(FilterFactory filterFactory) {
        FILTER_FACTORY = filterFactory;
    }

    public static FilterLoader getInstance() {
        return INSTANCE;
    }

    public int filerInstanceMapSize() {
        return filterRegistry.size();
    }

    public List<ZuulFilter> getFiltersByType(String filterType) {
        // 缓存命中则直接返回
        List<ZuulFilter> list = hashFiltersByType.get(filterType);
        if (list != null) {
            return list;
        }

        // 如果没有命中，则从注册表里面查询
        list = new ArrayList<>();
        Collection<ZuulFilter> allFilters = filterRegistry.getAllFilters();
        for (ZuulFilter filter : allFilters) {
            if (filter.filterType().equals(filterType)) {
                list.add(filter);
            }
        }
        Collections.sort(list);

        hashFiltersByType.putIfAbsent(filterType, list);
        return list;
    }
}
