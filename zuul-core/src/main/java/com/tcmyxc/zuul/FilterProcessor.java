package com.tcmyxc.zuul;


import com.netflix.servo.monitor.DynamicCounter;
import com.tcmyxc.zuul.context.Debug;
import com.tcmyxc.zuul.context.RequestContext;
import com.tcmyxc.zuul.exception.ZuulException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 执行过滤器的核心类
 */
public class FilterProcessor {

    protected static final Logger logger = LoggerFactory.getLogger(FilterProcessor.class);

    // 非单例
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
        List<ZuulFilter> list = FilterLoader.getInstance().getFiltersByType(filterType);
        if(list != null){
            for (int i = 0; i < list.size(); i++) {
                ZuulFilter filter = list.get(i);
                Object processedResult = processZuulFilter(filter);
                if(processedResult != null && processedResult instanceof Boolean){
                    result |= (Boolean) processedResult;
                }
            }
        }

        return result;
    }

    public Object processZuulFilter(ZuulFilter filter) throws ZuulException{
        RequestContext ctx = RequestContext.getCurrentContext();
        boolean debugged = ctx.debugRouting();
        final String metricPrefix = "zuul.filter-";
        long execTime = 0;
        String filterName = "";
        try {
            long startTime = System.currentTimeMillis();
            filterName = filter.getClass().getSimpleName();

            RequestContext copy = null;
            Object o = null;
            Throwable th = null;

            if(debugged){
                Debug.addRoutingDebug("Filter " + filter.filterType() + " " + filter.filterOrder() + " " + filterName);
                copy = ctx.copy();
            }

            ZuulFilterResult result = filter.runFilter();
            ExecutionStatus status = result.getStatus();
            execTime = System.currentTimeMillis() - startTime;

            switch (status){
                case FAILED:
                    th = result.getException();
                    ctx.addFilterExecutionSummary(filterName, ExecutionStatus.FAILED.name(), execTime);
                    break;
                case SUCCESS:
                    o = result.getResult();
                    ctx.addFilterExecutionSummary(filterName, ExecutionStatus.SUCCESS.name(), execTime);
                    if(debugged){
                        Debug.addRoutingDebug("Filter {" + filterName + " TYPE:" + filter.filterType() + " ORDER:" + filter.filterOrder() + "} Execution time = " + execTime + "ms");
                        Debug.compareContextState(filterName, copy);
                    }
                    break;
                default:
                    break;
            }

            if(th != null){
                throw th;
            }
            usageNotifier.notify(filter, status);
            return o;
        } catch (Throwable e) {
            if(debugged){
                Debug.addRoutingDebug("Running Filter failed " + filterName + " type:" + filter.filterType() + " order:" + filter.filterOrder() + " " + e.getMessage());
            }
            usageNotifier.notify(filter, ExecutionStatus.FAILED);
            if(e instanceof ZuulException){
                throw (ZuulException) e;
            }
            else {
                ZuulException ex = new ZuulException(e, "Filter threw Exception", 500, filter.filterType() + ":" + filterName);
                ctx.addFilterExecutionSummary(filterName, ExecutionStatus.FAILED.name(), execTime);
                throw ex;
            }
        }
    }

    public static class BasicFilterUsageNotifier implements FilterUsageNotifier{

        private static final String METRIC_PREFIX = "zuul.filter-";

        @Override
        public void notify(ZuulFilter filter, ExecutionStatus status) {
            DynamicCounter.increment(METRIC_PREFIX + filter.getClass().getSimpleName(), "status", status.name(), "filterType", filter.filterType());
        }
    }
}
