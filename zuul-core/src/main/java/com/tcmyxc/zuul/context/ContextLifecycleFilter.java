package com.tcmyxc.zuul.context;


import javax.servlet.*;
import java.io.IOException;

/**
 * 管理 RequestContext 的生命周期
 */
public class ContextLifecycleFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException, ServletException {
        try {
            filterChain.doFilter(req, resp);
        } finally {
            // 在一个请求完成之后清除上下文
            RequestContext.getCurrentContext().unset();
        }

    }

    @Override
    public void destroy() {

    }
}
