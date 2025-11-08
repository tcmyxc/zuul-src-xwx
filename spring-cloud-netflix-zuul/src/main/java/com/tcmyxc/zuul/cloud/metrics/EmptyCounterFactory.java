package com.tcmyxc.zuul.cloud.metrics;

import com.tcmyxc.zuul.monitoring.CounterFactory;

public class EmptyCounterFactory extends CounterFactory {
    @Override
    public void increment(String name) {

    }
}
