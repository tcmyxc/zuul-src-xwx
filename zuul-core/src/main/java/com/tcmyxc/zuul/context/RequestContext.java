package com.tcmyxc.zuul.context;


import com.tcmyxc.zuul.constants.ZuulHeaders;
import com.tcmyxc.zuul.util.DeepCopy;
import com.tcmyxc.zuul.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RequestContext extends ConcurrentHashMap<String, Object> {

    private static final Logger logger = LoggerFactory.getLogger(RequestContext.class);

    protected static Class<? extends RequestContext> contextClass = RequestContext.class;

    protected static final ThreadLocal<? extends RequestContext> threadLocal = new ThreadLocal<RequestContext>() {
        @Override
        protected RequestContext initialValue() {
            try {
                return contextClass.newInstance();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    };

    public RequestContext() {
        super();
    }

    public static void setContextClass(Class<? extends RequestContext> clazz) {
        contextClass = clazz;
    }

    public static RequestContext getCurrentContext() {
        RequestContext context = threadLocal.get();
        return context;
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultResponse) {
        Boolean b = (Boolean) get(key);
        if (b != null) {
            return b.booleanValue();
        }
        return defaultResponse;
    }

    public void set(String key, Object val) {
        if (val != null) {
            put(key, val);
        } else {
            remove(key);
        }
    }

    public void set(String key) {
        put(key, Boolean.TRUE);
    }

    public boolean getZuulEngineRan() {
        return getBoolean("zuulEngineRan");
    }

    public void setZuulEngineRan() {
        set("zuulEngineRan", true);
    }

    public HttpServletRequest getRequest() {
        return (HttpServletRequest) get("request");
    }

    public void setRequest(HttpServletRequest req) {
        put("request", req);
    }

    public void setResponse(HttpServletResponse resp) {
        put("response", resp);
    }

    public HttpServletResponse getResponse() {
        return (HttpServletResponse) get("response");
    }

    public Throwable getThrowable() {
        return (Throwable) get("throwable");
    }

    public void setThrowable(Throwable th) {
        set("throwable", th);
    }

    public void setDebugRouting(boolean val) {
        set("debugRouting", val);
    }

    public boolean debugRouting() {
        return getBoolean("debugRouting");
    }

    public void setDebugRequestHeadersOnly(boolean val) {
        set("debugRequestHeadersOnly", val);
    }

    public boolean debugRequestHeadersOnly() {
        return getBoolean("debugRequestHeadersOnly");
    }

    public void setDebugRequest(boolean val) {
        set("debugRequest", val);
    }

    public boolean debugRequest() {
        return getBoolean("debugRequest");
    }

    public void removeRouteHost() {
        remove("routeHost");
    }

    public void setRouteHost(URL routeHost) {
        set("routeHost", routeHost);
    }

    public URL getRouteHost() {
        return (URL) get("routeHost");
    }

    public StringBuilder getFilterExecutionSummary() {
        if (get("executedFilters") == null) {
            putIfAbsent("executedFilters", new StringBuilder());
        }
        return (StringBuilder) get("executedFilters");
    }

    public void addFilterExecutionSummary(String name, String status, long time) {
        StringBuilder sb = getFilterExecutionSummary();
        if (sb.length() > 0) sb.append(", ");
        sb.append(name).append('[').append(status).append(']').append('[').append(time).append("ms]");
    }

    public void setResponseBody(String body) {
        set("responseBody", body);
    }

    public String getResponseBody() {
        return (String) get("responseBody");
    }

    public void setResponseDataStream(InputStream responseDataStream) {
        set("responseDataStream", responseDataStream);
    }

    public InputStream getResponseDataStream() {
        return (InputStream) get("responseDataStream");
    }

    public void setResponseGZipped(boolean gzipped) {
        put("responseGZipped", gzipped);
    }

    public boolean getResponseGZipped() {
        return getBoolean("responseGZipped", true);
    }

    /**
     * <p>如果值为true，说明响应应该被发送到客户端
     * <p>如果值为false，说明已经有filter发送响应到客户端了，调用该方法的filter不需要执行run方法
     * @return
     */
    public boolean sendZuulResponse() {
        return getBoolean("sendZuulResponse", true);
    }

    public void setSendZuulResponse(boolean val) {
        set("sendZuulResponse", Boolean.valueOf(val));
    }

    public int getResponseStatusCode() {
        return get("responseStatusCode") != null ? (Integer) get("responseStatusCode") : 500;
    }

    public void setResponseStatusCode(int statusCode) {
        getResponse().setStatus(statusCode);
        set("responseStatusCode", statusCode);
    }

    public Map<String, String> getZuulRequestHeaders() {
        if (get("zuulRequestHeaders") == null) {
            HashMap<String, String> zuulRequestHeaders = new HashMap<>();
            putIfAbsent("zuulRequestHeaders", zuulRequestHeaders);
        }
        return (Map<String, String>) get("zuulRequestHeaders");
    }

    public void addZuulRequestHeader(String name, String value) {
        getZuulRequestHeaders().put(name.toLowerCase(), value);
    }

    public List<Pair<String, String>> getZuulResponseHeaders() {
        if (get("zuulResponseHeaders") == null) {
            List<Pair<String, String>> zuulRequestHeaders = new ArrayList<>();
            putIfAbsent("zuulResponseHeaders", zuulRequestHeaders);
        }
        return (List<Pair<String, String>>) get("zuulResponseHeaders");
    }

    public void addZuulResponseHeader(String name, String value) {
        getZuulResponseHeaders().add(new Pair<String, String>(name, value));
    }

    public List<Pair<String, String>> getOriginResponseHeaders() {
        if (get("originResponseHeaders") == null) {
            List<Pair<String, String>> originResponseHeaders = new ArrayList<>();
            putIfAbsent("originResponseHeaders", originResponseHeaders);
        }
        return (List<Pair<String, String>>) get("originResponseHeaders");
    }

    public void addOriginResponseHeader(String name, String value) {
        getOriginResponseHeaders().add(new Pair<String, String>(name, value));
    }

    public Long getOriginContentLength() {
        return (Long) get("originContentLength");
    }

    public void setOriginContentLength(Long v) {
        set("originContentLength", v);
    }

    public void setOriginContentLength(String v) {
        try {
            final Long i = Long.valueOf(v);
            setOriginContentLength(i);
        } catch (NumberFormatException e) {
            logger.warn("error parsing origin content length", e);
        }
    }

    public boolean isChunkedRequestBody() {
        return getBoolean("chunkedRequestBody");
    }

    public void setChunkedRequestBody() {
        set("chunkedRequestBody", Boolean.TRUE);
    }

    public boolean isGzipRequested() {
        final String requestEncoding = getRequest().getHeader(ZuulHeaders.ACCEPT_ENCODING);
        return requestEncoding != null && requestEncoding.toLowerCase().contains("gzip");
    }

    public void unset() {
        threadLocal.remove();
    }

    public void reset() {
        unset();
    }

    public RequestContext copy() {
        RequestContext copy = new RequestContext();
        Iterator<String> it = keySet().iterator();
        String key = it.next();
        while (key != null) {
            Object orig = get(key);
            try {
                Object copyValue = DeepCopy.copy(orig);
                if (copyValue != null) {
                    copy.set(key, copyValue);
                } else {
                    copy.set(key, orig);
                }
            } catch (NotSerializableException e) {
                copy.set(key, orig);
            }
            if (it.hasNext()) {
                key = it.next();
            } else {
                key = null;
            }
        }
        return copy;
    }

    public Map<String, List<String>> getRequestQueryParams() {
        return (Map<String, List<String>>) get("requestQueryParams");
    }

    public void setRequestQueryParams(Map<String, List<String>> qp) {
        put("requestQueryParams", qp);
    }


}
