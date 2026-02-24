package uk.ac.soton.comp2300.model.energy;

import java.io.InputStream;
import java.net.URI;
import java.net.http.*;
import java.nio.file.*;
import java.time.LocalDate;

public class RatesCacheManager {

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
        Files.copy(resp.body(), out, StandardCopyOption.REPLACE_EXISTING);
        return new RatesCacheResult(out, true);
    }

    public AgileRatesJsonProvider loadProviderFromCached(Path jsonPath) throws Exception {
        try (InputStream in = Files.newInputStream(jsonPath)) {
            return AgileRatesJsonProvider.fromStream(in);
        }
    }
}
