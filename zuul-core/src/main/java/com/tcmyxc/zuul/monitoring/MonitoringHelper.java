package com.tcmyxc.zuul.monitoring;


public class MonitoringHelper {


    private static final class CounterFactoryImpl extends CounterFactory {
        @Override
        public void increment(String name) {}
    }

    private static final class TracerFactoryImpl extends TracerFactory{
        @Override
        public Tracer startMicroTracer(String name) {
            return new TracerImpl();
        }
    }

    private static final class TracerImpl implements Tracer{

        @Override
        public void stopAndLog() {}

        @Override
        public void setName(String name) {}
    }
}
