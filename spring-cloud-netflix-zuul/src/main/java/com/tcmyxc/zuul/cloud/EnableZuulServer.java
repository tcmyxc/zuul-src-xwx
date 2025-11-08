package com.tcmyxc.zuul.cloud;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ZuulServerMarkerConfiguration.class)
public @interface EnableZuulServer {
}
