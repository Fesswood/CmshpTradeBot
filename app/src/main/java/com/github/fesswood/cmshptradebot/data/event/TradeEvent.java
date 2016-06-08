package com.github.fesswood.cmshptradebot.data.event;

/**
 * Created by fesswood on 04.06.16.
 */
public class TradeEvent {

    private String event;

    public TradeEvent(String event) {
        this.event = event;
    }

    public String getEvent() {
        return event;
    }
}
