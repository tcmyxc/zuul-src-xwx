package com.tcmyxc.zuul.gateway;

import com.tcmyxc.zuul.ZuulFilter;
import com.tcmyxc.zuul.context.RequestContext;
import com.tcmyxc.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;

/**
 * A simple route filter that proxies requests to http://127.0.0.1:8080
 */
public class RouteProxyFilter extends ZuulFilter {

    private static final Logger logger = LoggerFactory.getLogger(RouteProxyFilter.class);

    @Override
    public String filterType() {
        return "route";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        // Always route everything through this simple proxy
        return true;
    }

    @Override
    public Object run() throws ZuulException {
        try {
            logger.info("RouteProxyFilter.run() invoked");
            RequestContext ctx = RequestContext.getCurrentContext();
            HttpServletRequest req = ctx.getRequest();
            HttpServletResponse resp = ctx.getResponse();

            StringBuilder sb = new StringBuilder();
            sb.append("http://127.0.0.1:8080");
            sb.append(req.getRequestURI());
            if (req.getQueryString() != null && !req.getQueryString().isEmpty()) {
                sb.append("?").append(req.getQueryString());
            }

            URL url = new URL(sb.toString());
            logger.info("Proxying to URL: " + url);
            // 新建一个http请求示例（尚未发送请求）
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(req.getMethod());
            conn.setDoInput(true);// 允许读取响应数据

            // copy headers
            Enumeration<String> hn = req.getHeaderNames();
            while (hn != null && hn.hasMoreElements()) {
                String name = hn.nextElement();
                String val = req.getHeader(name);
                if (val != null) conn.setRequestProperty(name, val);
            }

            // copy request body for methods that have one
            if (req.getContentLength() > 0 || "POST".equalsIgnoreCase(req.getMethod()) || "PUT".equalsIgnoreCase(req.getMethod())) {
                conn.setDoOutput(true);// 允许向服务器写入数据（发送请求体）
                try (OutputStream os = conn.getOutputStream(); InputStream is = req.getInputStream()) {
                    byte[] buffer = new byte[4096];
                    int r;
                    while ((r = is.read(buffer)) != -1) {
                        os.write(buffer, 0, r);
                    }
                    os.flush();
                }
            }

            // 实际建立网络连接并发送HTTP请求
            int code = conn.getResponseCode();
            logger.info("Upstream response code: " + code);
            resp.setStatus(code);

            // copy response headers
            conn.getHeaderFields().forEach((k, v) -> {
                if (k != null && v != null) {
                    for (String hv : v) {
                        resp.addHeader(k, hv);
                    }
                }
            });

            // choose input stream (error or normal)
            InputStream cis = null;
            try {
                cis = (code >= 400) ? conn.getErrorStream() : conn.getInputStream();
                if (cis != null) {
                    try (OutputStream os = resp.getOutputStream()) {
                        byte[] buf = new byte[4096];
                        int len;
                        while ((len = cis.read(buf)) != -1) {
                            os.write(buf, 0, len);
                        }
                        os.flush();
                    }
                }
            } finally {
                if (cis != null) cis.close();
            }

            // mark that we've already written response
            ctx.setSendZuulResponse(false);
            logger.info("Finished proxying and wrote response to client");

            return null;
        } catch (Exception e) {
            throw new ZuulException(e, 500, "RouteProxyFilter failed");
        }
    }
}
