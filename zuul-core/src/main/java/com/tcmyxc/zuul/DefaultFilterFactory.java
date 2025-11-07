package com.tcmyxc.zuul;


public class DefaultFilterFactory implements FilterFactory{
    @Override
    public ZuulFilter newInstance(Class clazz) throws Exception {
        return (ZuulFilter) clazz.newInstance();
    }
}
