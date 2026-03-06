package uk.ac.soton.comp2300.recommendation_logic;
// Stores one time slot from the API

import java.time.LocalDateTime;

public class CarbonSlot {

    private LocalDateTime from;
    private LocalDateTime to;
    private int forecast;

    public CarbonSlot(LocalDateTime from, LocalDateTime to, int forecast){
        this.from = from;
        this.to = to;
        this.forecast = forecast;
    }

    public LocalDateTime getFrom(){
        return from;
    }

    public LocalDateTime getTo(){
        return to;
    }

    public int getForecast(){
        return forecast;
    }
    
}
