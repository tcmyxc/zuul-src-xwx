package com.tcmyxc.zuul.cloud.metrics;

import com.tcmyxc.zuul.monitoring.Tracer;
import com.tcmyxc.zuul.monitoring.TracerFactory;

public class EmptyTracerFactory extends TracerFactory {

    private final EmptyTracer emptyTracer = new EmptyTracer();

    @Override
    public Tracer startMicroTracer(String name) {
        return emptyTracer;
    }

    private static final class EmptyTracer implements Tracer{

        @Override
        public void stopAndLog() {

        }

        @Override
        public void setName(String name) {

        }
    }
}
