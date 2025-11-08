package com.tcmyxc.zuul.cloud;

import com.tcmyxc.zuul.context.RequestContext;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

public class ZuulServletFilter extends com.tcmyxc.zuul.filters.ZuulServletFilter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        RequestContext ctx = RequestContext.getCurrentContext();
        ctx.setZuulEngineRan();
        super.doFilter(servletRequest, servletResponse, filterChain);
    }
}
