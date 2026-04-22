package uk.ac.soton.comp2300.model.energy;

import com.google.gson.*;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
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
        RateSlot latestStartedSlot = null;
        for (RateSlot s : slots) {
            if (s.contains(time)) return s.getPencePerKwh();
            if (!time.isBefore(s.getStart())) {
                latestStartedSlot = s;
            }
        }
        if (latestStartedSlot != null) {
            return latestStartedSlot.getPencePerKwh();
        }
        if (!slots.isEmpty()) {
            return slots.get(0).getPencePerKwh();
        }
        throw new IllegalStateException("No Agile rate slots loaded.");
    }
}
