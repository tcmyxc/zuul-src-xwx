package com.tcmyxc.zuul.exception;


import com.tcmyxc.zuul.monitoring.CounterFactory;

public class ZuulException extends Exception {

    public int statusCode;

    public String errorCause;

    public ZuulException(Throwable throwable, String msg, int statusCode, String errorCause) {
        super(msg, throwable);
        this.errorCause = errorCause;
        this.statusCode = statusCode;
        incrementCounter("ZUUL::EXCEPTION:" + errorCause + ":" + statusCode);
    }

    public ZuulException(String msg, int statusCode, String errorCause) {
        super(msg);
        this.statusCode = statusCode;
        this.errorCause = errorCause;
        incrementCounter("ZUUL::EXCEPTION:" + errorCause + ":" + statusCode);
    }

    public ZuulException(Throwable throwable, int statusCode, String errorCause) {
        super(throwable.getMessage(), throwable);
        this.statusCode = statusCode;
        this.errorCause = errorCause;
        incrementCounter("ZUUL::EXCEPTION:" + errorCause + ":" + statusCode);
    }

    private static final void incrementCounter(String name) {
        CounterFactory.instance().increment(name);
    }

}
