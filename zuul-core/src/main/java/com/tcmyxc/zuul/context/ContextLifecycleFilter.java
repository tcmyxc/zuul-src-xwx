package com.tcmyxc.zuul.context;


import javax.servlet.*;
import java.io.IOException;

public class ContextLifecycleFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException, ServletException {
        try {
            filterChain.doFilter(req, resp);
        } finally {
            RequestContext.getCurrentContext().unset();
        }

    }

    @Override
    public void destroy() {

    }
}
