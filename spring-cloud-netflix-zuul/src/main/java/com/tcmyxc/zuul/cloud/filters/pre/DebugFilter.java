package com.tcmyxc.zuul.cloud.filters.pre;

import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.tcmyxc.zuul.ZuulFilter;
import com.tcmyxc.zuul.constants.ZuulConstants;
import com.tcmyxc.zuul.context.RequestContext;
import com.tcmyxc.zuul.exception.ZuulException;

import javax.servlet.http.HttpServletRequest;

import static com.tcmyxc.zuul.cloud.filters.support.FilterConstants.DEBUG_FILTER_ORDER;
import static com.tcmyxc.zuul.cloud.filters.support.FilterConstants.PRE_TYPE;

public class DebugFilter extends ZuulFilter {

    private static final DynamicBooleanProperty ROUTING_DEBUG = DynamicPropertyFactory
            .getInstance().getBooleanProperty(ZuulConstants.ZUUL_DEBUG_REQUEST, false);

    private static final DynamicStringProperty DEBUG_PARAMETER = DynamicPropertyFactory
            .getInstance().getStringProperty(ZuulConstants.ZUUL_DEBUG_PARAMETER, "debug");

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return DEBUG_FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
        if("true".equals(request.getParameter(DEBUG_PARAMETER.get()))){
            return true;
        }
        return ROUTING_DEBUG.get();
    }

    @Override
    public Object run() throws ZuulException {
        RequestContext ctx = RequestContext.getCurrentContext();
        ctx.setDebugRequest(true);
        ctx.setDebugRouting(true);
        return null;
    }
}
