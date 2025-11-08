package com.tcmyxc.zuul;


import com.tcmyxc.zuul.exception.ZuulException;

/**
 * done
 */
public interface IZuulFilter {

    boolean shouldFilter();

    Object run() throws ZuulException;
}
