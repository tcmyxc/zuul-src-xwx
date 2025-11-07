package com.tcmyxc.zuul;


import com.netflix.servo.monitor.DynamicCounter;
import com.tcmyxc.zuul.context.Debug;
import com.tcmyxc.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 执行过滤器的核心类
 */
public class FilterProcessor {

    protected static final Logger logger = LoggerFactory.getLogger(FilterProcessor.class);

    // 饿汉式单例
    static FilterProcessor instance = new FilterProcessor();

    private FilterUsageNotifier usageNotifier;

    public FilterProcessor() {
        usageNotifier = new BasicFilterUsageNotifier();
    }

    public static FilterProcessor getInstance(){
        return instance;
    }

    public static void setProcessor(FilterProcessor processor){
        instance = processor;
    }

    public void setFilterUsageNotifier(FilterUsageNotifier usageNotifier) {
        this.usageNotifier = usageNotifier;
    }

    public Object runFilters(String filterType) throws Throwable{
        if(RequestContext.getCurrentContext().debugRouting()){
            Debug.addRoutingDebug("Invoking {" + filterType + "} type filters");
        }

        boolean result = false;
    }

    public static class BasicFilterUsageNotifier implements FilterUsageNotifier{

        private static final String METRIC_PREFIX = "zuul.filter-";

        @Override
        public void notify(ZuulFilter filter, ExecutionStatus status) {
            DynamicCounter.increment(METRIC_PREFIX + filter.getClass().getSimpleName(), "status", status.name(), "filterType", filter.filterType());
        }
    }
}
