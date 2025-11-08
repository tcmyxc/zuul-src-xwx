package com.tcmyxc.zuul;

/**
 * done
 */
public interface FilterFactory {

    ZuulFilter newInstance(Class clazz) throws Exception;
}
