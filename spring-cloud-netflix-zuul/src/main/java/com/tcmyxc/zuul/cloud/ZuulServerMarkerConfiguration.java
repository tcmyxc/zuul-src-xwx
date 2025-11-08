package com.tcmyxc.zuul.cloud;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ZuulServerMarkerConfiguration {

    @Bean
    public Marker zuulServerMarkerBean() {
        return new Marker();
    }

    class Marker {

    }

}

