package uk.ac.soton.comp2300.model.energy;

import com.google.gson.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// uses https://api.carbonintensity.org.uk/ for carbon intensity data (gCO2/kWh) in the UK

public class CarbonIntensityClient {

    private static final ZoneId UK_ZONE = ZoneId.of("Europe/London");

    public static class IntensitySlot {
        public ZonedDateTime from;
        public ZonedDateTime to;
        public double kgPerKwh;

        public IntensitySlot(ZonedDateTime from, ZonedDateTime to, double kgPerKwh) {
            this.from = from;
            this.to = to;
            this.kgPerKwh = kgPerKwh;
        }

        public boolean contains(ZonedDateTime t) {
            return !t.isBefore(from) && t.isBefore(to);
        }
    }

    private Path assetsDir;
    private HttpClient http;

    public CarbonIntensityClient(Path assetsDir) {
        this.assetsDir = assetsDir;
        this.http = HttpClient.newHttpClient();
    }

    private void ensureAssetsDir() throws Exception {
        Files.createDirectories(assetsDir);
    }

    private Path cachePath(LocalDate date) {
        return assetsDir.resolve("carbon_intensity_" + date + ".json");
    }

    public Path updateDaily(LocalDate date) throws Exception {
        ensureAssetsDir();
        Path out = cachePath(date);
        if (Files.exists(out)) return out;

        LocalDate todayUk = LocalDate.now(UK_ZONE);
        String url = date.equals(todayUk)
                ? "https://api.carbonintensity.org.uk/intensity/date"
                : "https://api.carbonintensity.org.uk/intensity/date/" + date;
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .GET()
                .header("User-Agent", "EcoSphereDev/1.0")
                .build();

        HttpResponse<InputStream> resp = http.send(req, HttpResponse.BodyHandlers.ofInputStream());
        if (resp.statusCode() / 100 != 2) {
            throw new IllegalStateException("Failed to download carbon intensity: HTTP " + resp.statusCode());
        }
        saveNormalizedDay(resp.body(), out, date);
        return out;
    }

    public List<IntensitySlot> loadDay(LocalDate date, ZoneId zone) throws Exception {
        updateDaily(date);
        Path p = cachePath(date);

        try (InputStream in = Files.newInputStream(p)) {
            JsonObject root = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonArray data = root.getAsJsonArray("data");

            List<IntensitySlot> out = new ArrayList<>();
            if (data == null) return out;

            for (JsonElement el : data) {
                JsonObject item = el.getAsJsonObject();
                ZonedDateTime from = ZonedDateTime.parse(item.get("from").getAsString()).withZoneSameInstant(zone);
                ZonedDateTime to = ZonedDateTime.parse(item.get("to").getAsString()).withZoneSameInstant(zone);

                JsonObject intensity = item.getAsJsonObject("intensity");
                JsonElement actual = intensity.get("actual");
                JsonElement forecast = intensity.get("forecast");

                double gPerKwh = (actual != null && !actual.isJsonNull()) ? actual.getAsDouble() : forecast.getAsDouble();
                double kgPerKwh = gPerKwh / 1000.0;

                out.add(new IntensitySlot(from, to, kgPerKwh));
            }
            return out;
        }
    }

    public double intensityKgPerKwhAt(LocalDate date, ZonedDateTime time, ZoneId zone) throws Exception {
        List<IntensitySlot> slots = loadDay(date, zone);
        IntensitySlot latestStartedSlot = null;
        for (IntensitySlot s : slots) {
            if (s.contains(time)) return s.kgPerKwh;
            if (!time.isBefore(s.from)) {
                latestStartedSlot = s;
            }
        }
        if (latestStartedSlot != null) {
            return latestStartedSlot.kgPerKwh;
        }
        if (!slots.isEmpty()) {
            return slots.get(0).kgPerKwh;
        }
        return 0.20;
    }

    /**
     * Infer carbon peak as the highest-intensity contiguous 3-hour block (6 half-hour slots).
     */
    public PeakWindow inferPeakWindowForDate(LocalDate date, ZoneId zone) throws Exception {
        List<IntensitySlot> day = loadDay(date, zone);
        if (day.isEmpty()) {
            return new PeakWindow(LocalTime.of(16, 0), LocalTime.of(19, 0));
        }

        day.sort(Comparator.comparing(s -> s.from));

        int windowSlots = Math.min(6, day.size());
        int bestStartIdx = 0;
        double bestTotalIntensity = Double.NEGATIVE_INFINITY;

        for (int i = 0; i <= day.size() - windowSlots; i++) {
            double totalIntensity = 0.0;
            for (int j = 0; j < windowSlots; j++) {
                totalIntensity += day.get(i + j).kgPerKwh;
            }
            if (totalIntensity > bestTotalIntensity) {
                bestTotalIntensity = totalIntensity;
                bestStartIdx = i;
            }
        }

        LocalTime start = day.get(bestStartIdx).from.withZoneSameInstant(zone).toLocalTime();
        LocalTime end = day.get(bestStartIdx + windowSlots - 1).to.withZoneSameInstant(zone).toLocalTime();

        if (!start.isBefore(end)) {
            return new PeakWindow(LocalTime.of(16, 0), LocalTime.of(19, 0));
        }
        return new PeakWindow(start, end);
    }

    private void saveNormalizedDay(InputStream body, Path out, LocalDate date) throws Exception {
        JsonObject root;
        try (InputStreamReader reader = new InputStreamReader(body, StandardCharsets.UTF_8)) {
            root = JsonParser.parseReader(reader).getAsJsonObject();
        }

        JsonArray filteredData = new JsonArray();
        JsonArray data = root.getAsJsonArray("data");
        if (data != null) {
            for (JsonElement element : data) {
                JsonObject slot = element.getAsJsonObject();
                if (isForRequestedDate(slot, date)) {
                    filteredData.add(slot);
                }
            }
        }

        root.add("data", filteredData);
        Files.writeString(out, root.toString(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private boolean isForRequestedDate(JsonObject slot, LocalDate date) {
        JsonElement fromElement = slot.get("from");
        if (fromElement == null || fromElement.isJsonNull()) {
            return false;
        }

        ZonedDateTime from = ZonedDateTime.parse(fromElement.getAsString()).withZoneSameInstant(UK_ZONE);
        return from.toLocalDate().equals(date);
    }
}
