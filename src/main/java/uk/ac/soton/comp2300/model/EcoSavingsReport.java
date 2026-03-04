package uk.ac.soton.comp2300.model;

import uk.ac.soton.comp2300.model.energy.*;

public class EcoSavingsReport {

    private CostAndCarbonResult current;      // scheduled time
    private CostAndCarbonResult peak;         // peak baseline time
    private double moneySavedPounds;          // peak - current
    private double co2SavedKg;               // peak - current
    private PeakWindow peakWindow;            // peak window used

    public EcoSavingsReport() {}

    public EcoSavingsReport(CostAndCarbonResult current,
                               CostAndCarbonResult peak,
                               double moneySavedPounds,
                               double co2SavedKg,
                               PeakWindow peakWindow) {
        this.current = current;
        this.peak = peak;
        this.moneySavedPounds = moneySavedPounds;
        this.co2SavedKg = co2SavedKg;
        this.peakWindow = peakWindow;
    }

    public CostAndCarbonResult getCurrent() { return current; }
    public CostAndCarbonResult getPeak() { return peak; }
    public double getMoneySavedPounds() { return moneySavedPounds; }
    public double getCo2SavedKg() { return co2SavedKg; }
    public PeakWindow getPeakWindow() { return peakWindow; }
    public EcoSavingsReport(double moneySavedPounds, double co2SavedKg) {
        this.moneySavedPounds = moneySavedPounds;
        this.co2SavedKg = co2SavedKg;
        // Set other fields to null or default since they aren't needed for the dashboard
        this.current = null;
        this.peak = null;
        this.peakWindow = null;
    }
}