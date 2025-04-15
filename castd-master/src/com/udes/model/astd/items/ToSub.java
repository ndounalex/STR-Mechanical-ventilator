package com.udes.model.astd.items;

public class ToSub extends Arrow {

    private String s1;
    private String s2;
    private String s2b;

    public ToSub (String s1, String s2b, String s2) {
        this.s1 = s1;
        this.s2 = s2;
        this.s2b = s2b;
    }

    public String getS1() {
        return s1;
    }

    public void setS1(String s1) {
        this.s1 = s1;
    }

    public String getS2() { return s2; }

    public void setS2(String s2) {
        this.s2 = s2;
    }

    public String getS2b() {
        return s2b;
    }

    public void setS2b(String s2b) {
        this.s2b = s2b;
    }
}
