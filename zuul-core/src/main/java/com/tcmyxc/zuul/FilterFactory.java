package com.tcmyxc.zuul;


public interface FilterFactory {

    ZuulFilter newInstance(Class clazz) throws Exception;
}
