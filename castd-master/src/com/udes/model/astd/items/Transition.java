package com.udes.model.astd.items;


public class Transition {

    private Arrow arrow;
    private Event event;
    private String guard;
    private Action action;
    private boolean isfinal;

    public Transition(Arrow arrow, Event event, String guard, Action action, boolean isfinal) {
        this.arrow = arrow;
        this.event = event;
        this.guard = guard;
        this.action = action;
        this.isfinal = isfinal;
    }

    public Transition() {}

    public Arrow getArrow() {
        return arrow;
    }

    public void setArrow(Arrow arrow) {
        this.arrow = arrow;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getGuard() {
        return guard;
    }

    public void setGuard(String guard) {
        this.guard = guard;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public boolean isFinal() {
        return isfinal;
    }

    public void setFinal(boolean isfinal) {
        this.isfinal = isfinal;
    }
}
