package com.tcmyxc.zuul.cloud.util;

import com.tcmyxc.zuul.context.RequestContext;

import static com.tcmyxc.zuul.cloud.filters.support.FilterConstants.IS_DISPATCHER_SERVLET_REQUEST_KEY;

public class RequestUtils {

    private RequestUtils() {
        throw new AssertionError("Must not instantiate utility class.");
    }

    public static boolean isDispatcherServletRequest() {
        return RequestContext.getCurrentContext().getBoolean(IS_DISPATCHER_SERVLET_REQUEST_KEY);
    }

    public static boolean isZuulServletRequest() {
        // extra check for dispatcher since ZuulServlet can run from ZuulController
        return !isDispatcherServletRequest() && RequestContext.getCurrentContext().getZuulEngineRan();
    }
}
