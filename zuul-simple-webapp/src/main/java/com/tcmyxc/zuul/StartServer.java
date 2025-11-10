package com.tcmyxc.zuul;


import com.tcmyxc.zuul.context.ContextLifecycleFilter;
import com.tcmyxc.zuul.context.RequestContext;
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

@SpringBootApplication
public class StartServer {

    private static final Logger logger = LoggerFactory.getLogger(StartServer.class);

    public static void main(String[] args) {
        SpringApplication.run(StartServer.class, args);

        MonitoringHelper.initMocks();

        initJavaFilters();

    }

    @Bean
    public ServletRegistrationBean zuulServlet() {
        // Register the core ZuulServlet from zuul-core so the full pre/route/post pipeline runs
        ServletRegistrationBean registration = new ServletRegistrationBean(new ZuulServlet(), "/*");
        registration.addInitParameter("buffer-requests", "false");
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


    private static void initJavaFilters() {
        final FilterRegistry r = FilterRegistry.instance();

        r.put("javaPreFilter", new ZuulFilter() {
            @Override
            public int filterOrder() {
                return 1;
            }

            @Override
            public String filterType() {
                return "pre";
            }

            @Override
            public boolean shouldFilter() {
                return true;
            }

            @Override
            public Object run() {
                logger.info("running javaPreFilter");
                RequestContext.getCurrentContext().set("javaPreFilter-ran", true);
                return null;
            }
        });

        r.put("javaPostFilter", new ZuulFilter() {
            @Override
            public int filterOrder() {
                return 10000;
            }

            @Override
            public String filterType() {
                return "post";
            }

            @Override
            public boolean shouldFilter() {
                return true;
            }

            @Override
            public Object run() {
                logger.info("running javaPostFilter");
                RequestContext.getCurrentContext().set("javaPostFilter-ran", true);
                return null;
            }
        });
    }

}
