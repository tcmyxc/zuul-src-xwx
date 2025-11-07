package com.tcmyxc.zuul.monitoring;


public abstract class TracerFactory {

    private static TracerFactory instance;

    public static final void initialize(TracerFactory f) {
        instance = f;
    }


    public static final TracerFactory instance() {
        if(instance == null) throw new IllegalStateException(String.format("%s not initialized", TracerFactory.class.getSimpleName()));
        return instance;
    }

    public abstract Tracer startMicroTracer(String name);
}
