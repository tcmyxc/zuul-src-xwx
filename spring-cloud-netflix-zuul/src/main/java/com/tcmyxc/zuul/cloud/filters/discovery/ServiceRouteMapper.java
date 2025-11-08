package com.tcmyxc.zuul.cloud.filters.discovery;

public interface ServiceRouteMapper {

    /**
     * Take a service Id (its discovered name) and return a route path.
     * @param serviceId service discovered name
     * @return route path
     */
    String apply(String serviceId);
}
