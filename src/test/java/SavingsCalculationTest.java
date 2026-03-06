import org.junit.jupiter.api.Test;
import uk.ac.soton.comp2300.model.EcoSavingsReport;
import uk.ac.soton.comp2300.model.energy.CostAndCarbonResult;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Extended SavingsCalculationTest: Validates device-specific impact
 * and data integrity for the Dashboard.
 */
public class SavingsCalculationTest {

    @Test
    void testHighEnergyDeviceImpact() {
        // Simulating a high-energy device (e.g., Dryer)
        // Peak: 5.0 kWh, GBP 0.75 cost, 1.0kg CO2
        CostAndCarbonResult peak = new CostAndCarbonResult(5.0, 0.75, 1.0);
        // Scheduled: 5.0 kWh, GBP 0.45 cost, 0.6kg CO2
        CostAndCarbonResult scheduled = new CostAndCarbonResult(5.0, 0.45, 0.6);

        EcoSavingsReport report = new EcoSavingsReport(scheduled, peak, 0.30, 0.4, null);

        // Verify the values displayed in Dashboard's 'createImpactRow'
        assertEquals(0.30, report.getMoneySavedPounds(), "Dryer money savings incorrect");
        assertEquals(0.4, report.getCo2SavedKg(), "Dryer carbon offset incorrect");
    }

    @Test
    void testNegativeSavingsScenario() {
        // Testing a scenario where scheduled time is actually worse than baseline
        // Peak (Baseline): GBP 0.20 | Scheduled: GBP 0.30
        CostAndCarbonResult peak = new CostAndCarbonResult(1.0, 0.20, 0.2);
        CostAndCarbonResult scheduled = new CostAndCarbonResult(1.0, 0.30, 0.3);

        double moneySaved = peak.getCostPounds() - scheduled.getCostPounds();

        // Dashboard should ideally handle negative values or show 0
        assertTrue(moneySaved < 0, "Logic should reflect a loss if scheduled during peak");
        assertEquals(-0.10, moneySaved, 0.001);
    }

    @Test
    void testDashboardDisplayPrecision() {
        // Dashboard uses String.format("%.2f") for display
        double complexValue = 0.12857;
        CostAndCarbonResult peak = new CostAndCarbonResult(1.0, complexValue, 0.5);
        CostAndCarbonResult scheduled = new CostAndCarbonResult(1.0, 0.0, 0.5);

        EcoSavingsReport report = new EcoSavingsReport(scheduled, peak, complexValue, 0.0, null);

        // Simulate the Dashboard's formatting logic
        String displayedMoney = String.format("\u00A3%.2f", report.getMoneySavedPounds());

        assertEquals("\u00A30.13", displayedMoney, "Dashboard rounding logic mismatch");
    }
}
