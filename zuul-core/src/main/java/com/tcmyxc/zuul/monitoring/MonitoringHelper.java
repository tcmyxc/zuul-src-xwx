package com.tcmyxc.zuul.monitoring;


public class MonitoringHelper {

    public static final void initMocks() {

        CounterFactory.init(new EmptyCounterFactory());
        TracerFactory.initialize(new EmptyTracerFactory());
    }
}
