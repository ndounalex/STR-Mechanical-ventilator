package com.udes.model.il.containers;

import java.io.Serializable;

public class Entry<T, U> implements Serializable {
    private T key;
    private U value;

    public Entry(T key, U value) {
        this.key = key;
        this.value = value;
    }

    public Entry() {}

    public T getKey() {
        return key;
    }

    public void setKey(T key) {
        this.key = key;
    }

    public U getValue() {
        return value;
    }

    public void setValue(U value) {
        this.value = value;
    }

    public boolean isEmpty() {
        return (key == null) && (value == null);
    }
}
