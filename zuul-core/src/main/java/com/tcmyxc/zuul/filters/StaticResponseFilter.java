package com.tcmyxc.zuul.filters;

import com.tcmyxc.zuul.ZuulFilter;
import com.tcmyxc.zuul.context.RequestContext;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.regex.Pattern;

public abstract class StaticResponseFilter extends ZuulFilter {

    /**
     * Define a URI eg /static/content/path or List of URIs for this filter to return a static response.
     * @return String URI or java.util.List of URIs
     */
    public abstract Object uri();

    public abstract String responseBody();

    @Override
    public String filterType() {
        return "static";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        String path = RequestContext.getCurrentContext().getRequest().getRequestURI();
        if (checkPath(path)) return true;
        return checkPath("/" + path);
    }

    /**
     * checks if the path matches the uri()
     * @param path usually the RequestURI()
     * @return true if the pattern matches
     */
    boolean checkPath(String path) {
        Object uri = uri();
        if (uri instanceof String) {
            return uri.equals(path);
        } else if (uri instanceof List) {
            return ((List<?>) uri).contains(path);
        } else if (uri instanceof Pattern) {
            return ((Pattern) uri).matcher(path).matches();
        }
        return false;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        // Set the default response code for static filters to be 200
        ctx.setResponseStatusCode(HttpServletResponse.SC_OK);
        // first StaticResponseFilter instance to match wins, others do not set body and/or status
        if (ctx.getResponseBody() == null) {
            ctx.setResponseBody(responseBody());
            ctx.setSendZuulResponse(false);
        }
        return null;
    }

}

