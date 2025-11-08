package com.tcmyxc.zuul;


import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import com.tcmyxc.zuul.monitoring.Tracer;
import com.tcmyxc.zuul.monitoring.TracerFactory;

import java.util.concurrent.atomic.AtomicReference;

/**
 * done
 */
public abstract class ZuulFilter implements IZuulFilter, Comparable<ZuulFilter> {

    private final AtomicReference<DynamicBooleanProperty> filterDisabledRef = new AtomicReference<>();

    abstract public String filterType();

    abstract public int filterOrder();

    public boolean isStaticFilter() {
        return true;
    }

    public String disablePropertyName() {
        return "zuul." + this.getClass().getSimpleName() + "." + filterType() + ".disable";
    }

    public boolean isFilterDisabled() {
        filterDisabledRef.compareAndSet(null, DynamicPropertyFactory.getInstance().getBooleanProperty(disablePropertyName(), false));
        return filterDisabledRef.get().get();
    }

    public ZuulFilterResult runFilter() {
        ZuulFilterResult zr = new ZuulFilterResult();
        if (!isFilterDisabled()) {
            if (shouldFilter()) {
                Tracer t = TracerFactory.instance().startMicroTracer("ZUUL::" + this.getClass().getSimpleName());
                try {
                    Object res = run();
                    zr = new ZuulFilterResult(res, ExecutionStatus.SUCCESS);
                } catch (Throwable e) {
                    t.setName("ZUUL::" + this.getClass().getSimpleName() + " failed");
                    zr = new ZuulFilterResult(ExecutionStatus.FAILED);
                    zr.setException(e);
                } finally {
                    t.stopAndLog();
                }
            } else {
                zr = new ZuulFilterResult(ExecutionStatus.SKIPPED);
            }
        }
        return zr;
    }

    @Override
    public int compareTo(ZuulFilter other) {
        return Integer.compare(this.filterOrder(), other.filterOrder());
    }
}
