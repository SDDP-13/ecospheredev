package uk.ac.soton.comp2300.model.energy;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class RatesCacheManager {

    private static final ZoneId UK_ZONE = ZoneId.of("Europe/London");
    private Path assetsDir;
    private HttpClient http;

    public RatesCacheManager(Path assetsDir) {
        this.assetsDir = assetsDir;
        this.http = HttpClient.newHttpClient();
    }

    private void ensureAssetsDir() throws Exception {
        Files.createDirectories(assetsDir);
    }

    public Path cachePath(String regionLetter, LocalDate date) {
        String r = (regionLetter == null ? "A" : regionLetter.toUpperCase());
        return assetsDir.resolve("agile_region_" + r + "_" + date + ".json");
    }

    /** Returns (path, downloadedNow). */
    public RatesCacheResult updateDaily(String regionLetter, LocalDate date) throws Exception {
        ensureAssetsDir();
        Path out = cachePath(regionLetter, date);
        if (Files.exists(out)) return new RatesCacheResult(out, false);

        String url = RegionEndpoints.urlForRegion(regionLetter);
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .GET()
                .header("User-Agent", "EcoSphereDev/1.0")
                .build();

        HttpResponse<InputStream> resp = http.send(req, HttpResponse.BodyHandlers.ofInputStream());
        if (resp.statusCode() / 100 != 2) {
            throw new IllegalStateException("Failed to download Agile rates: HTTP " + resp.statusCode());
        }
        saveNormalizedRates(resp.body(), out, date);
        return new RatesCacheResult(out, true);
    }

    public AgileRatesJsonProvider loadProviderFromCached(Path jsonPath) throws Exception {
        try (InputStream in = Files.newInputStream(jsonPath)) {
            return AgileRatesJsonProvider.fromStream(in);
        }
    }

    private void saveNormalizedRates(InputStream body, Path out, LocalDate date) throws Exception {
        JsonObject root;
        try (InputStreamReader reader = new InputStreamReader(body, StandardCharsets.UTF_8)) {
            root = JsonParser.parseReader(reader).getAsJsonObject();
        }

        JsonArray filteredRates = new JsonArray();
        JsonArray rates = root.getAsJsonArray("rates");
        if (rates != null) {
            for (JsonElement element : rates) {
                JsonObject rate = element.getAsJsonObject();
                if (isForRequestedDate(rate, date)) {
                    filteredRates.add(rate);
                }
            }
        }

        root.add("rates", filteredRates);
        Files.writeString(out, root.toString(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private boolean isForRequestedDate(JsonObject rate, LocalDate date) {
        JsonElement startElement = rate.get("deliveryStart");
        if (startElement == null || startElement.isJsonNull()) {
            return false;
        }

        ZonedDateTime start = ZonedDateTime.parse(startElement.getAsString()).withZoneSameInstant(UK_ZONE);
        return start.toLocalDate().equals(date);
    }
}
