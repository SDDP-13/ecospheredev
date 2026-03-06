package uk.ac.soton.comp2300.recommendation_logic;
// Calls the carbon intensity API and gets the forecast data

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CarbonIntensityService {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public List<CarbonSlot> get24HourForecast() throws Exception{
        String from = LocalDateTime.now()
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm'Z'"));
        
        String url = "https://api.carbonintensity.org.uk/regional/intensity/" + from + "/fw24h/postcode/SO17";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Accept", "application/json")
            .GET()
            .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Carbon API URL: " + url);
        System.out.println("Carbon API status: " + response.statusCode());
        System.out.println("Carbon API body: " + response.body());

        if (response.statusCode() != 200){
            throw new IOException("Carbon API returned status " + response.statusCode());
        }
        JsonNode root = MAPPER.readTree(response.body());
        JsonNode outerData = root.path("data");

        if (!outerData.isArray() || outerData.isEmpty()){
            throw new IOException("Carbon API returned no data.");
        }

        JsonNode regionBlock = outerData.get(0);
        JsonNode data = regionBlock.path("data");

        if (!data.isArray() || data.isEmpty()){
            throw new IOException("Carbon API returned no forecast slots.");
        }

        List<CarbonSlot> slots = new ArrayList<>();

        for (JsonNode slot : data){
            String fromText = slot.path("from").asText();
            String toText = slot.path("to").asText();
            int forecast = slot.path("intensity").path("forecast").asInt();
        
            LocalDateTime fromTime = OffsetDateTime.parse(fromText).toLocalDateTime();
            LocalDateTime toTime = OffsetDateTime.parse(toText).toLocalDateTime();
        
            slots.add(new CarbonSlot(fromTime, toTime, forecast));
        }
        return slots;
    }
    
}
