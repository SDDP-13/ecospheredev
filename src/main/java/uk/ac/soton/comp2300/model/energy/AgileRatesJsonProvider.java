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
     * Peak window inferred from the most expensive contiguous block.
     * Uses a sliding window of 6 half-hours (= 3 hours).
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

        // sort by time, then pick the highest-sum contiguous 3-hour block
        day.sort(Comparator.comparing(RateSlot::getStart));

        int windowSlots = Math.min(6, day.size());
        int bestStartIdx = 0;
        double bestTotalRate = Double.NEGATIVE_INFINITY;

        for (int i = 0; i <= day.size() - windowSlots; i++) {
            double totalRate = 0.0;
            for (int j = 0; j < windowSlots; j++) {
                totalRate += day.get(i + j).getPencePerKwh();
            }
            if (totalRate > bestTotalRate) {
                bestTotalRate = totalRate;
                bestStartIdx = i;
            }
        }

        LocalTime start = day.get(bestStartIdx).getStart().withZoneSameInstant(zone).toLocalTime();
        LocalTime end = day.get(bestStartIdx + windowSlots - 1).getEnd().withZoneSameInstant(zone).toLocalTime();

        if (!start.isBefore(end)) {
            return new PeakWindow(LocalTime.of(16, 0), LocalTime.of(19, 0));
        }
        return new PeakWindow(start, end);
    }
}
