package uk.ac.soton.comp2300.model.energy;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public interface RatesProvider {
    double ratePencePerKwh(ZonedDateTime time);

    /** infer peak window from prices (top N expensive half-hours) */
    PeakWindow inferPeakWindowForDate(LocalDate date, ZoneId zone);
}
