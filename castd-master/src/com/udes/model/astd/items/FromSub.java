package com.udes.model.astd.items;

public class FromSub extends Arrow{

    private String s1;
    private String s1b;
    private String s2;

    public FromSub (String s1b, String s2, String s1) {
        this.s1 = s1;
        this.s1b = s1b;
        this.s2 = s2;
    }
    public String getS1() { return s1; }

    public void setS1(String s1) {
        this.s1 = s1;
    }

    public String getS1b() {
        return s1b;
    }

    public void setS1b(String s1b) {
        this.s1b = s1b;
    }

    public String getS2() {
        return s2;
    }

    public void setS2(String s2) {
        this.s2 = s2;
    }
}
