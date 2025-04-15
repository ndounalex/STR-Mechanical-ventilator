package com.udes.model.astd.items;

public class ActionSet {
    private Action entry;
    private Action stay;
    private Action exit;

    public ActionSet() {}
    public ActionSet(Action entry, Action stay, Action exit) {
        this.stay = stay;
        this.entry = entry;
        this.exit = exit;
    }

    public Action getEntry() {
        return entry;
    }

    public void setEntry(Action entry) {
        this.entry = entry;
    }

    public Action getExit() {
        return exit;
    }

    public void setExit(Action exit) {
        this.exit = exit;
    }

    public Action getStay() {
        return stay;
    }

    public void setStay(Action stay) {
        this.stay = stay;
    }
}
