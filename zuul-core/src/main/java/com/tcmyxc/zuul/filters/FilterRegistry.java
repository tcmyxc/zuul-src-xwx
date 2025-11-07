package com.tcmyxc.zuul.filters;


import com.tcmyxc.zuul.ZuulFilter;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class FilterRegistry {

    // 饿汉式单例
    private static final FilterRegistry instance = new FilterRegistry();

    private final ConcurrentHashMap<String, ZuulFilter> filters = new ConcurrentHashMap<>();

    private FilterRegistry(){}

    public static final FilterRegistry instance(){
        return instance;
    }

    public ZuulFilter remove(String key){
        return filters.remove(key);
    }

    public ZuulFilter get(String key){
        return filters.get(key);
    }

    public void put(String key, ZuulFilter filter){
        filters.putIfAbsent(key, filter);
    }

    public int size(){
        return filters.size();
    }

    public Collection<ZuulFilter> getAllFilters(){
        return filters.values();
    }
}
