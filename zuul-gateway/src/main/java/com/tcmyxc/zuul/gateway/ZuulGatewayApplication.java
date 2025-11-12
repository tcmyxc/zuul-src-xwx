package com.tcmyxc.zuul.gateway;

import com.tcmyxc.zuul.context.ContextLifecycleFilter;
import com.tcmyxc.zuul.filters.FilterRegistry;
import com.tcmyxc.zuul.http.ZuulServlet;
import com.tcmyxc.zuul.monitoring.MonitoringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class ZuulGatewayApplication {

    private static final Logger logger = LoggerFactory.getLogger(ZuulGatewayApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ZuulGatewayApplication.class, args);
    }

    @Bean
    public ServletRegistrationBean zuulServlet() {
        // Register the core ZuulServlet from zuul-core so the full pre/route/post pipeline runs
        ServletRegistrationBean registration = new ServletRegistrationBean(new ZuulServlet(), "/*");
        // 暴露这个filter的意思就在于提供一个不缓存请求的路由
        registration.addInitParameter("buffer-requests", "false");
        registration.setName("zuulServlet");
        registration.setLoadOnStartup(1);
        return registration;
    }

    // zuulServlet 和 zuulServletFilter 仅需要配置其中一个就行，spring cloud 推荐配置 zuulServlet
    // @Bean
    // public FilterRegistrationBean zuulServletFilter(){
    //     final FilterRegistrationBean<ZuulServletFilter> filterRegistration = new FilterRegistrationBean<>();
    //     filterRegistration.addUrlPatterns("/*");
    //     filterRegistration.setFilter(new ZuulServletFilter());
    //     filterRegistration.setOrder(Ordered.LOWEST_PRECEDENCE);
    //     filterRegistration.addInitParameter("buffer-requests", "false");
    //     return filterRegistration;
    // }

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
