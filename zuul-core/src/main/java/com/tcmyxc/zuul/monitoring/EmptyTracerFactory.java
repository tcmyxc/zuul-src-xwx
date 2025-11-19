package com.tcmyxc.zuul.monitoring;


public class EmptyTracerFactory extends TracerFactory {
    @Override
    public Tracer startMicroTracer(String name) {
        return new EmptyTracer();
    }


    private static final class EmptyTracer implements Tracer {

        @Override
        public void stopAndLog() {
        }

        @Override
        public void setName(String name) {
        }
    }
}
