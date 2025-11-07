package com.tcmyxc.zuul;


public interface FilterUsageNotifier {

    void notify(ZuulFilter filter, ExecutionStatus status);
}
