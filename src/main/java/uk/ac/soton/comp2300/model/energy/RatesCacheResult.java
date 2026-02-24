package uk.ac.soton.comp2300.model.energy;

import java.nio.file.Path;

public class RatesCacheResult {
    private Path path;
    private boolean downloaded;

    public RatesCacheResult() {}

    public RatesCacheResult(Path path, boolean downloaded) {
        this.path = path;
        this.downloaded = downloaded;
    }

    public Path getPath() { return path; }
    public boolean isDownloaded() { return downloaded; }
}
