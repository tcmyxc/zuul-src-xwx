package com.tcmyxc.zuul;

/**
 * done
 */
public class DefaultFilterFactory implements FilterFactory{
    @Override
    public ZuulFilter newInstance(Class clazz) throws Exception {
        return (ZuulFilter) clazz.newInstance();
    }
}
