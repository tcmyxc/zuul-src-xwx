package com.tcmyxc.zuul.util;


import com.tcmyxc.zuul.context.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HTTPRequestUtils {

    private static final HTTPRequestUtils INSTANCE = new HTTPRequestUtils();

    public static final String X_FORWARDED_FOR_HEADER = "x-forwarded-for";

    private HTTPRequestUtils() {
    }

    ;

    public static HTTPRequestUtils getInstance() {
        return INSTANCE;
    }

    public String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader(X_FORWARDED_FOR_HEADER);
        String clientIP = null;
        if (xForwardedFor == null) {
            clientIP = request.getRemoteAddr();
        } else {
            clientIP = extractClientIpFromXForwardedFor(xForwardedFor);
        }
        return clientIP;
    }

    private String extractClientIpFromXForwardedFor(String xForwardedFor) {
        if (xForwardedFor == null) {
            return null;
        }
        xForwardedFor = xForwardedFor.trim();
        String[] tokenized = xForwardedFor.split(";");
        if (tokenized.length == 0) {
            return null;
        } else {
            return tokenized[0].trim();
        }
    }

    /**
     * 获取当前请求中headerName对应的值
     *
     * @param headerName
     * @return
     */
    public String getHeaderValue(String headerName) {
        return RequestContext.getCurrentContext().getRequest().getHeader(headerName);
    }

    public String getFormValue(String headerName) {
        return RequestContext.getCurrentContext().getRequest().getParameter(headerName);
    }

    public Map<String, List<String>> getRequestHeaderMap(){
        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
        Map<String, List<String>> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null){
            
        }
    }


}
