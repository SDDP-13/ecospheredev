package uk.ac.soton.comp2300.model.energy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;

public class PeakWindowCache {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private Path assetsDir;

    public PeakWindowCache(Path assetsDir) {
        this.assetsDir = assetsDir;
    }

    public Path peakPath(String regionLetter, LocalDate date) {
        String r = (regionLetter == null ? "A" : regionLetter.toUpperCase());
        return assetsDir.resolve("peak_region_" + r + "_" + date + ".json");
    }

    public boolean exists(String regionLetter, LocalDate date) {
        return Files.exists(peakPath(regionLetter, date));
    }

    public void write(String regionLetter, LocalDate date, PeakWindow peak) throws Exception {
        Files.createDirectories(assetsDir);
        String json = GSON.toJson(peak);
        Files.writeString(peakPath(regionLetter, date), json, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public PeakWindow read(String regionLetter, LocalDate date) throws Exception {
        String json = Files.readString(peakPath(regionLetter, date), StandardCharsets.UTF_8);
        return GSON.fromJson(json, PeakWindow.class);
    }
}