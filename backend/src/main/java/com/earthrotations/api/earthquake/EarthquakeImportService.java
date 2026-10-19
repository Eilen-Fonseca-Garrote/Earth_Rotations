package com.earthrotations.api.earthquake;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.earthrotations.api.country.CountryBoundaryService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class EarthquakeImportService {
    private static final String USGS_FEED =
            "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_day.geojson";

    private final EarthquakeRepository repository;
    private final ObjectMapper objectMapper;
    private final CountryBoundaryService countryBoundaryService;
    private final RestClient restClient;

    public EarthquakeImportService(EarthquakeRepository repository, ObjectMapper objectMapper,
            CountryBoundaryService countryBoundaryService) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.countryBoundaryService = countryBoundaryService;
        this.restClient = RestClient.create();
    }

    @Scheduled(fixedDelayString = "${earthquakes.sync-delay-ms:600000}", initialDelayString = "${earthquakes.sync-initial-delay-ms:5000}")
    public void scheduledImport() {
        importLatest();
    }

    public int importLatest() {
        String payload = restClient.get()
                .uri(USGS_FEED)
                .retrieve()
                .body(String.class);

        try {
            JsonNode features = objectMapper.readTree(payload).path("features");
            List<Earthquake> earthquakes = new ArrayList<>();

            for (JsonNode feature : features) {
                Earthquake earthquake = mapFeature(feature);
                if (earthquake != null) {
                    earthquakes.add(earthquake);
                }
            }

            repository.saveAll(earthquakes);
            return earthquakes.size();
        } catch (Exception exception) {
            throw new IllegalStateException("No se pudo interpretar la respuesta de USGS", exception);
        }
    }

    private Earthquake mapFeature(JsonNode feature) {
        JsonNode properties = feature.path("properties");
        JsonNode coordinates = feature.path("geometry").path("coordinates");

        if (!feature.hasNonNull("id") || properties.path("time").isNull()
                || properties.path("mag").isNull() || !coordinates.isArray() || coordinates.size() < 3) {
            return null;
        }

        Earthquake earthquake = new Earthquake();
        earthquake.setId(feature.path("id").asText());
        earthquake.setMagnitude(properties.path("mag").asDouble());
        earthquake.setLongitude(coordinates.get(0).asDouble());
        earthquake.setLatitude(coordinates.get(1).asDouble());
        earthquake.setDepth(coordinates.get(2).asDouble());
        earthquake.setPlace(properties.path("place").asText("Ubicación desconocida"));
        earthquake.setOccurredAt(Instant.ofEpochMilli(properties.path("time").asLong()));
        earthquake.setCountry(countryBoundaryService.findCountryCode(earthquake.getLatitude(), earthquake.getLongitude()));
        earthquake.setSourceUrl(properties.path("url").asText(null));
        return earthquake;
    }
}