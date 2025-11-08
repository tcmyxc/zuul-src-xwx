package com.tcmyxc.zuul.cloud.filters.discovery;

public class SimpleServiceRouteMapper implements ServiceRouteMapper{
    @Override
    public String apply(String serviceId) {
        return serviceId;
    }
}
