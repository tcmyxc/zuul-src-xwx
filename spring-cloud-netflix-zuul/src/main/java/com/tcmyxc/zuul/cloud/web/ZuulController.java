package com.tcmyxc.zuul.cloud.web;

import com.tcmyxc.zuul.context.RequestContext;
import com.tcmyxc.zuul.http.ZuulServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ServletWrappingController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ZuulController extends ServletWrappingController {

    public ZuulController() {
        setServletClass(ZuulServlet.class);
        setServletName("zuul");
        setSupportedMethods((String[]) null); // Allow all
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        try {
            // We don't care about the other features of the base class, just want to handle the request
            return super.handleRequestInternal(request, response);
        } finally {
            // 清除上下文
            RequestContext.getCurrentContext().unset();
        }
    }
}
