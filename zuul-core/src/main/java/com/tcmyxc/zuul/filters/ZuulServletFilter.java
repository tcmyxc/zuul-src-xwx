package com.tcmyxc.zuul.filters;

import com.tcmyxc.zuul.ZuulRunner;
import com.tcmyxc.zuul.context.RequestContext;
import com.tcmyxc.zuul.exception.ZuulException;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ZuulServletFilter 和 ZuulServlet 两个用一个就行，但是Spring推荐用ZuulServlet
 */
public class ZuulServletFilter implements Filter {
    private ZuulRunner zuulRunner;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        String bufferReqsStr = filterConfig.getInitParameter("buffer-requests");
        boolean bufferReqs = bufferReqsStr != null && bufferReqsStr.equals("true");

        zuulRunner = new ZuulRunner(bufferReqs);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            init((HttpServletRequest) servletRequest, (HttpServletResponse) servletResponse);
            try {
                preRouting();
            } catch (ZuulException e) {
                error(e);
                postRouting();
                return;
            }

            // Only forward onto to the chain if a zuul response is not being sent
            if (!RequestContext.getCurrentContext().sendZuulResponse()) {
                filterChain.doFilter(servletRequest, servletResponse);
                return;
            }

            try {
                routing();
            } catch (ZuulException e) {
                error(e);
                postRouting();
                return;
            }
            try {
                postRouting();
            } catch (ZuulException e) {
                error(e);
                return;
            }
        } catch (Throwable e) {
            error(new ZuulException(e, 500, "UNCAUGHT_EXCEPTION_FROM_FILTER_" + e.getClass().getName()));
        } finally {
            RequestContext.getCurrentContext().unset();
        }
    }

    void postRouting() throws ZuulException {
        zuulRunner.postRoute();
    }

    void routing() throws ZuulException {
        zuulRunner.route();
    }

    void preRouting() throws ZuulException {
        zuulRunner.preRoute();
    }

    void init(HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        zuulRunner.init(servletRequest, servletResponse);
    }

    void error(ZuulException e) {
        RequestContext.getCurrentContext().setThrowable(e);
        zuulRunner.error();
    }

    @Override
    public void destroy() {
    }
}
