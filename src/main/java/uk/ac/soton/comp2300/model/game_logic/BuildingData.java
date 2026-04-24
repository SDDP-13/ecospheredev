package uk.ac.soton.comp2300.model.game_logic;

public class BuildingData {

    private BuildingType type;
    private int level;
    private double theta;
    private double phi;

    public BuildingData(BuildingType type, double theta, double phi) {
        this.type = type;
        this.level = 1;
        this.theta = theta;
        this.phi = phi;
    }

    public BuildingType getType() { return type; }
    public int getLevel() { return level; }
    public void levelUp() { if (level < 5) level++; }
    public double getTheta() { return theta; }
    public double getPhi() { return phi; }
}
