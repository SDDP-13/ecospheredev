package uk.ac.soton.comp2300.model.energy;

public class CostAndCarbonResult {
    private double kwh;
    private double costPounds;
    private double co2Kg;

    public CostAndCarbonResult() {}

    public CostAndCarbonResult(double kwh, double costPounds, double co2Kg) {
        this.kwh = kwh;
        this.costPounds = costPounds;
        this.co2Kg = co2Kg;
    }

    public double getKwh() { return kwh; }
    public double getCostPounds() { return costPounds; }
    public double getCo2Kg() { return co2Kg; }
}
