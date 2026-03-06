package uk.ac.soton.comp2300.model.energy;

import com.google.gson.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.lang.reflect.Type;
import java.util.Objects;

public class PeakWindowCache {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalTime.class, new JsonSerializer<LocalTime>() {
                @Override
                public JsonElement serialize(LocalTime src, Type typeOfSrc, JsonSerializationContext context) {
                    return new JsonPrimitive(src.toString());
                }
            })
            .registerTypeAdapter(LocalTime.class, new JsonDeserializer<LocalTime>() {
                @Override
                public LocalTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                        throws JsonParseException {
                    return LocalTime.parse(json.getAsString());
                }
            })
            .setPrettyPrinting()
            .create();

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
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(peak, "peak must not be null");
        Objects.requireNonNull(peak.getStartInclusive(), "peak.startInclusive must not be null");
        Objects.requireNonNull(peak.getEndExclusive(), "peak.endExclusive must not be null");

        Path out = peakPath(regionLetter, date);
        Files.createDirectories(out.getParent());

        String json = "{\"startInclusive\":\"" + peak.getStartInclusive() +
                "\",\"endExclusive\":\"" + peak.getEndExclusive() + "\"}";

        Files.write(out, json.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
    }

    public PeakWindow read(String regionLetter, LocalDate date) throws Exception {
        String json = Files.readString(peakPath(regionLetter, date), StandardCharsets.UTF_8);
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        LocalTime start = LocalTime.parse(obj.get("startInclusive").getAsString());
        LocalTime end = LocalTime.parse(obj.get("endExclusive").getAsString());
        return new PeakWindow(start, end);
    }
}
