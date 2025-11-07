package com.tcmyxc.zuul.context;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Debug {

    public static void setDebugRequest(boolean debugRequest) {
        RequestContext.getCurrentContext().setDebugRequest(debugRequest);
    }

    public static void setDebugRequestHeadersOnly(boolean headersOnly) {
        RequestContext.getCurrentContext().setDebugRequestHeadersOnly(headersOnly);
    }

    public static boolean debugRequestHeadersOnly() {
        return RequestContext.getCurrentContext().debugRequestHeadersOnly();
    }

    public static void setDebugRouting(boolean debugRouting) {
        RequestContext.getCurrentContext().setDebugRouting(debugRouting);
    }

    public static boolean debugRequest() {
        return RequestContext.getCurrentContext().debugRequest();
    }

    public static boolean debugRouting() {
        return RequestContext.getCurrentContext().debugRouting();
    }

    public static List<String> getRoutingDebug() {
        List<String> rd = (List<String>) RequestContext.getCurrentContext().get("routingDebug");
        if (rd == null) {
            rd = new ArrayList<>();
            RequestContext.getCurrentContext().set("routingDebug", rd);
        }
        return rd;
    }

    public static void addRoutingDebug(String line) {
        List<String> rd = getRoutingDebug();
        rd.add(line);
    }

    public static List<String> getRequestDebug() {
        List<String> rd = (List<String>) RequestContext.getCurrentContext().get("requestDebug");
        if (rd == null) {
            rd = new ArrayList<>();
            RequestContext.getCurrentContext().set("requestDebug", rd);
        }
        return rd;
    }

    public static void addRequestDebug(String line) {
        List<String> rd = getRequestDebug();
        rd.add(line);
    }

    public static void compareContextState(String filterName, RequestContext copy) {
        RequestContext context = RequestContext.getCurrentContext();
        Iterator<String> it = context.keySet().iterator();
        String key = it.next();
        while (key != null) {
            if ((!key.equals("routingDebug") && !key.equals("requestDebug"))) {
                Object newValue = context.get(key);
                Object oldValue = copy.get(key);
                if (oldValue == null && newValue != null) {
                    addRoutingDebug("{" + filterName + "} added " + key + "=" + newValue);
                } else if (oldValue != null && newValue != null) {
                    if (!(oldValue.equals(newValue))) {
                        addRoutingDebug("{" + filterName + "} changed " + key + "=" + newValue);
                    }
                }
            }
            if (it.hasNext()) {
                key = it.next();
            } else {
                key = null;
            }
        }

    }
}
