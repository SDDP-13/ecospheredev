package uk.ac.soton.comp2300.model.energy;

import java.time.ZonedDateTime;

public interface RatesProvider {
    double ratePencePerKwh(ZonedDateTime time);
}
