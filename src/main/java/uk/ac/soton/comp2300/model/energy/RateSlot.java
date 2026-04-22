package uk.ac.soton.comp2300.model.energy;

import java.time.ZonedDateTime;

public class RateSlot {
    private ZonedDateTime start;
    private ZonedDateTime end;
    private double pencePerKwh;

    public RateSlot() {}

    public RateSlot(ZonedDateTime start, ZonedDateTime end, double pencePerKwh) {
        this.start = start;
        this.end = end;
        this.pencePerKwh = pencePerKwh;
    }

    public ZonedDateTime getStart() { return start; }
    public ZonedDateTime getEnd() { return end; }
    public double getPencePerKwh() { return pencePerKwh; }

    public boolean contains(ZonedDateTime t) {
        return !t.isBefore(start) && t.isBefore(end);
    }
}
