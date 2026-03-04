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
import java.util.List;

// uses https://api.carbonintensity.org.uk/ for carbon intensity data (gCO2/kWh) in the UK

public class CarbonIntensityClient {

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
            return (t.equals(from) || t.isAfter(from)) && t.isBefore(to);
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

        String url = "https://api.carbonintensity.org.uk/intensity/date/" + date;
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .GET()
                .header("User-Agent", "EcoSphereDev/1.0")
                .build();

        HttpResponse<InputStream> resp = http.send(req, HttpResponse.BodyHandlers.ofInputStream());
        if (resp.statusCode() / 100 != 2) {
            throw new IllegalStateException("Failed to download carbon intensity: HTTP " + resp.statusCode());
        }
        Files.copy(resp.body(), out, StandardCopyOption.REPLACE_EXISTING);
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
        for (IntensitySlot s : slots) {
            if (s.contains(time)) return s.kgPerKwh;
        }
        if (slots.isEmpty()) return 0.20;

        double sum = 0.0;
        for (IntensitySlot s : slots) sum += s.kgPerKwh;
        return sum / slots.size();
    }
}