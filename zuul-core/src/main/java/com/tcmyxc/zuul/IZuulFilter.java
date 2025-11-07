package com.tcmyxc.zuul;


import com.tcmyxc.zuul.exception.ZuulException;

public interface IZuulFilter {

    boolean shouldFilter();

    Object run() throws ZuulException;
}
