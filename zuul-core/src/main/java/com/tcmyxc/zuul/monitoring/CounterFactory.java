package com.tcmyxc.zuul.monitoring;


public abstract class CounterFactory {

    private static CounterFactory instance;

    public static final void init(CounterFactory counterFactory){
        instance = counterFactory;
    }

    public static final CounterFactory instance(){
        if(instance == null) throw new IllegalStateException(String.format("%s not initialized", CounterFactory.class.getSimpleName()));
        return instance;
    }

    public abstract void increment(String name);
}
