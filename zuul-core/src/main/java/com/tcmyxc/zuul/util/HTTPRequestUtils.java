package com.tcmyxc.zuul.util;


import com.tcmyxc.zuul.context.RequestContext;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.util.*;

/**
 * done
 */
public class HTTPRequestUtils {

    private static final HTTPRequestUtils INSTANCE = new HTTPRequestUtils();

    public static final String X_FORWARDED_FOR_HEADER = "x-forwarded-for";

    private HTTPRequestUtils() {
    }

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

    public Map<String, List<String>> getRequestHeaderMap() {
        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
        Map<String, List<String>> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String val = request.getHeader(headerName);

                if (StringUtils.isNotEmpty(headerName) && val != null) {
                    List<String> valList = new ArrayList<>();
                    if (headers.containsKey(headerName)) {
                        headers.get(headerName).add(val);
                    }
                    valList.add(val);
                    headers.put(headerName, valList);
                }
            }
        }
        return Collections.unmodifiableMap(headers);
    }

    public Map<String, List<String>> getQueryParams() {

        Map<String, List<String>> qp = RequestContext.getCurrentContext().getRequestQueryParams();
        if (qp != null) return qp;

        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
        // 如果请求路径里面不带参数，则直接返回
        if (request.getQueryString() == null) return null;

        qp = new LinkedHashMap<>();
        StringTokenizer st = new StringTokenizer(request.getQueryString(), "&");
        int i;
        while (st.hasMoreTokens()) {
            // 形式为 key=val
            String s = st.nextToken();
            i = s.indexOf("=");
            if (i > 0 && s.length() >= i + 1) {
                String name = s.substring(0, i);
                String value = s.substring(i + 1);

                try {
                    name = URLDecoder.decode(name, "UTF-8");
                } catch (Exception e) {
                }
                try {
                    value = URLDecoder.decode(value, "UTF-8");
                } catch (Exception e) {
                }

                List<String> valueList = qp.get(name);
                if (valueList == null) {
                    valueList = new LinkedList<>();
                    qp.put(name, valueList);
                }

                valueList.add(value);
            } else if (i == -1) {
                String name = s;
                String value = "";
                try {
                    name = URLDecoder.decode(name, "UTF-8");
                } catch (Exception e) {
                }

                List<String> valueList = qp.get(name);
                if (valueList == null) {
                    valueList = new LinkedList<>();
                    qp.put(name, valueList);
                }

                valueList.add(value);

            }
        }

        RequestContext.getCurrentContext().setRequestQueryParams(qp);
        return qp;
    }

    public String getValueFromRequestElements(String name) {
        String val = null;
        if (getQueryParams() != null) {
            final List<String> v = getQueryParams().get(name);
            if (v != null && !v.isEmpty()) val = v.iterator().next();
        }
        if (val != null) return val;
        val = getHeaderValue(name);
        if (val != null) return val;
        val = getFormValue(name);
        return val;
    }

    public boolean isGzipped(String contentEncoding) {
        return contentEncoding.contains("gzip");
    }


}
