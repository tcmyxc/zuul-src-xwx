package com.tcmyxc.zuul;


import com.tcmyxc.zuul.filters.FilterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class FilterLoader {

    private static final Logger logger = LoggerFactory.getLogger(FilterLoader.class);

    static final FilterLoader instance = new FilterLoader();

    private final ConcurrentHashMap<String, Long> filterClassLastModified = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> filterClassCode = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> filterCheck = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<ZuulFilter>> hashFiltersByType = new ConcurrentHashMap<>();

    private FilterRegistry filterRegistry = FilterRegistry.instance();

    static DynamicCodeCompiler COMPILER;

    static FilterFactory FILTER_FACTORY = new DefaultFilterFactory();

    public void setFilterRegistry(FilterRegistry filterRegistry) {
        this.filterRegistry = filterRegistry;
    }

    public static void setCompiler(DynamicCodeCompiler compiler) {
        COMPILER = compiler;
    }

    public static void setFilterFactory(FilterFactory filterFactory) {
        FILTER_FACTORY = filterFactory;
    }

    public static FilterLoader getInstance() {
        return instance;
    }

    public ZuulFilter getFilter(String code, String name) throws Exception {
        if (filterCheck.get(name) == null) {
            filterCheck.putIfAbsent(name, name);
            if (!code.equals(filterClassCode.get(name))) {
                logger.info("reloading code " + name);
                filterRegistry.remove(name);
            }
        }

        ZuulFilter filter = filterRegistry.get(name);
        if (filter == null) {
            Class clazz = COMPILER.compile(code, name);
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                filter = FILTER_FACTORY.newInstance(clazz);
            }
        }
        return filter;
    }

    public int filerInstanceMapSize() {
        return filterRegistry.size();
    }

    /**
     * 从文件中读取ZuulFilter源码，编译之后加入过滤器列表
     */
    public boolean putFilter(File file) throws Exception {
        String name = file.getAbsolutePath() + file.getName();
        Long modifiedTime = filterClassLastModified.get(name);
        // 如果该文件被修改过，而且最近的修改时间和记录的不一致，则先卸载原来的filter
        if (modifiedTime != null && (file.lastModified() != modifiedTime)) {
            logger.debug("reloading filter " + name);
            filterRegistry.remove(name);
        }
        ZuulFilter filter = filterRegistry.get(name);
        if (filter == null) {
            Class clazz = COMPILER.compile(file);
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                filter = FILTER_FACTORY.newInstance(clazz);
                List<ZuulFilter> list = hashFiltersByType.get(filter.filterType());
                // TODO 为何要移除
                if (list != null) {
                    hashFiltersByType.remove(filter.filterType());
                }
                // 放到缓存里面
                filterRegistry.put(name, filter);
                filterClassLastModified.put(name, file.lastModified());
                return true;
            }
        }
        return false;
    }

    public List<ZuulFilter> getFiltersByType(String filterType) {
        List<ZuulFilter> list = hashFiltersByType.get(filterType);
        if (list == null) {
            return list;
        }

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
