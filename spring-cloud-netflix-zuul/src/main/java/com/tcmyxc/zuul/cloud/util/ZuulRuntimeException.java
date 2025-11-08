package com.tcmyxc.zuul.cloud.util;

import com.tcmyxc.zuul.exception.ZuulException;

public class ZuulRuntimeException extends RuntimeException {

    public ZuulRuntimeException(ZuulException cause) {
        super(cause);
    }

    @Deprecated
    public ZuulRuntimeException(Exception ex) {
        super(ex);
    }

}
