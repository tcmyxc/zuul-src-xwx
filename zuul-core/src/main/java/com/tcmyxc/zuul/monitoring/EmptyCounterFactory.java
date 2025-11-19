package com.tcmyxc.zuul.monitoring;


public class EmptyCounterFactory extends CounterFactory {
    @Override
    public void increment(String name) {
    }
}
