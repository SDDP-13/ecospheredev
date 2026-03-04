package uk.ac.soton.comp2300.model.energy;

import java.time.LocalTime;

public class PeakWindow {
    private LocalTime startInclusive;
    private LocalTime endExclusive;

    public PeakWindow() {}

    public PeakWindow(LocalTime startInclusive, LocalTime endExclusive) {
        this.startInclusive = startInclusive;
        this.endExclusive = endExclusive;
    }

    public LocalTime getStartInclusive() { return startInclusive; }
    public LocalTime getEndExclusive() { return endExclusive; }

    public boolean contains(LocalTime t) {
        return !t.isBefore(startInclusive) && t.isBefore(endExclusive);
    }
}
