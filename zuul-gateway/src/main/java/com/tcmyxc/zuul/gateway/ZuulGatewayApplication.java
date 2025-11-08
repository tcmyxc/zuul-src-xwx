package com.tcmyxc.zuul.gateway;

import com.tcmyxc.zuul.context.ContextLifecycleFilter;
import com.tcmyxc.zuul.filters.FilterRegistry;
import com.tcmyxc.zuul.filters.ZuulServletFilter;
import com.tcmyxc.zuul.monitoring.CounterFactory;
import com.tcmyxc.zuul.monitoring.MonitoringHelper;
import com.tcmyxc.zuul.monitoring.TracerFactory;
import com.tcmyxc.zuul.monitoring.Tracer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import com.tcmyxc.zuul.http.ZuulServlet;
import javax.servlet.Servlet;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class ZuulGatewayApplication {

    private static final Logger logger = LoggerFactory.getLogger(ZuulGatewayApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ZuulGatewayApplication.class, args);
    }

    @Bean
    public ServletRegistrationBean zuulServletRegistration() {
        // Register the core ZuulServlet from zuul-core so the full pre/route/post pipeline runs
        ServletRegistrationBean registration = new ServletRegistrationBean(new ZuulServlet(), "/*");
        registration.addInitParameter("buffer-requests", "true");
        registration.setName("zuulServlet");
        registration.setLoadOnStartup(1);
        return registration;
    }

    @Bean
    public FilterRegistrationBean contextLifecycleFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new ContextLifecycleFilter());
        registration.addUrlPatterns("/*");
        registration.setName("contextLifecycleFilter");
        return registration;
    }

    @PostConstruct
    public void initFilters() {
        MonitoringHelper.initMocks();

        try {
            // instantiate and register the route filter so it will be picked up by the core ZuulServlet
            RouteProxyFilter routeFilter = new RouteProxyFilter();
            FilterRegistry.instance().put(RouteProxyFilter.class.getName(), routeFilter);
        } catch (Throwable e) {
            // ignore registration failures but log to stdout
            logger.error("Failed to register RouteProxyFilter: " + e.getMessage());
        }
    }
}
