package com.matthew.railboard;

public class Departure {
    public String time;        // scheduled
    public String expected;    // live estimate
    public String destination;
    public String platform;
    public String operator;
    public boolean cancelled;
    public boolean isDemo;

    public String expectedText() {
        if (cancelled) return "Cancelled";
        if (expected == null || expected.length() == 0 || expected.equalsIgnoreCase(time)) return "On time";
        return expected;
    }
}