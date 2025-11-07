package com.tcmyxc.zuul.util;


import java.io.Serializable;
import java.util.Objects;

public class Pair<E1,E2> implements Serializable {

    private static final long serialVersionUID = 2L;

    private E1 first;

    private E2 second;

    public Pair() {
    }

    public Pair(E1 first, E2 second) {
        this.first = first;
        this.second = second;
    }

    public E2 second() {
        return second;
    }

    public void setSecond(E2 second) {
        this.second = second;
    }

    public E1 first() {
        return first;
    }

    public void setFirst(E1 first) {
        this.first = first;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Pair<?, ?> other = (Pair<?, ?>) o;
        return Objects.equals(first, other.first) && Objects.equals(second, other.second);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(first);
        result = 31 * result + Objects.hashCode(second);
        return result;
    }
}
