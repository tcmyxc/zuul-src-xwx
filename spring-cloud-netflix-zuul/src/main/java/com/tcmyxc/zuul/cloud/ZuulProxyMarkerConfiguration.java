package com.tcmyxc.zuul.cloud;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZuulProxyMarkerConfiguration {

    @Bean
    public Marker zuulProxyMarkerBean() {
        return new Marker();
    }

    class Marker {

    }

}
