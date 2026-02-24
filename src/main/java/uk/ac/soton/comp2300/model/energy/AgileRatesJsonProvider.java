package uk.ac.soton.comp2300.model.energy;

import com.google.gson.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.*;

// a reader for the Agile Rates JSON format

public class AgileRatesJsonProvider implements RatesProvider {

    private List<RateSlot> slots = new ArrayList<>();

    public AgileRatesJsonProvider() {}

    public AgileRatesJsonProvider(List<RateSlot> slots) {
        this.slots = new ArrayList<>(slots);
        this.slots.sort(Comparator.comparing(RateSlot::getStart));
    }

    public static AgileRatesJsonProvider fromStream(InputStream in) throws Exception {
        JsonElement rootEl = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        JsonObject root = rootEl.getAsJsonObject();

        List<RateSlot> out = new ArrayList<>();
        JsonArray rates = root.getAsJsonArray("rates");
        if (rates != null) {
            for (JsonElement e : rates) {
                JsonObject r = e.getAsJsonObject();

                ZonedDateTime start = ZonedDateTime.parse(r.get("deliveryStart").getAsString());
                ZonedDateTime end = ZonedDateTime.parse(r.get("deliveryEnd").getAsString());

                // agileRate.result.rate in pence per kWh
                double rate = r.getAsJsonObject("agileRate")
                               .getAsJsonObject("result")
                               .get("rate").getAsDouble();

                out.add(new RateSlot(start, end, rate));
            }
        }
        return new AgileRatesJsonProvider(out);
    }

    @Override
    public double ratePencePerKwh(ZonedDateTime time) {
        for (RateSlot s : slots) {
            if (s.contains(time)) return s.getPencePerKwh();
        }
        if (!slots.isEmpty()) return slots.get(slots.size() - 1).getPencePerKwh();
        throw new IllegalStateException("No Agile rate slots loaded.");
    }

    /**
     * Peak window inferred from “most expensive sort”:
     * take top N most expensive half-hours (default 6 => 3 hours),
     * peak window = earliest start to latest end among those top slots.
     */
    @Override
    public PeakWindow inferPeakWindowForDate(LocalDate date, ZoneId zone) {
        List<RateSlot> day = new ArrayList<>();
        for (RateSlot s : slots) {
            LocalDate localDate = s.getStart().withZoneSameInstant(zone).toLocalDate();
            if (localDate.equals(date)) day.add(s);
        }
        if (day.isEmpty()) {
            return new PeakWindow(LocalTime.of(16, 0), LocalTime.of(19, 0));
        }

        // sort by price (descending)
        day.sort((a, b) -> Double.compare(b.getPencePerKwh(), a.getPencePerKwh()));

        int topN = Math.min(6, day.size()); // 6 * 0.5 hours = 3 hours
        LocalTime earliest = null;
        LocalTime latest = null;

        for (int i = 0; i < topN; i++) {
            RateSlot s = day.get(i);
            LocalTime st = s.getStart().withZoneSameInstant(zone).toLocalTime();
            LocalTime en = s.getEnd().withZoneSameInstant(zone).toLocalTime();

            if (earliest == null || st.isBefore(earliest)) earliest = st;
            if (latest == null || en.isAfter(latest)) latest = en;
        }

        // fallback
        if (earliest == null || latest == null || !earliest.isBefore(latest)) {
            return new PeakWindow(LocalTime.of(16, 0), LocalTime.of(19, 0));
        }
        return new PeakWindow(earliest, latest);
    }
}
