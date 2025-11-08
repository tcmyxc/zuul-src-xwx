package com.tcmyxc.zuul;

/**
 * done
 */
public interface FilterUsageNotifier {

    void notify(ZuulFilter filter, ExecutionStatus status);
}
