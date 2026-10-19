package com.earthrotations.api.country;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class CountryBoundaryService {
    private static final String BOUNDARIES_URL =
            "https://raw.githubusercontent.com/datasets/geo-countries/master/data/countries.geojson";
        private static final String COUNTRY_CATALOG_URL =
            "https://raw.githubusercontent.com/mledoze/countries/master/countries.json";

    private final ObjectMapper objectMapper;
    private final RestClient restClient = RestClient.create();
    private volatile JsonNode features;
    private volatile JsonNode catalog;

    public CountryBoundaryService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String findCountryCode(double latitude, double longitude) {
        JsonNode loadedFeatures = loadFeatures();
        for (JsonNode feature : loadedFeatures) {
            JsonNode geometry = feature.path("geometry");
            if (isInsideGeometry(geometry, longitude, latitude)) {
                return feature.path("properties").path("ISO3166-1-Alpha-2").asText(null);
            }
        }
        return null;
    }

    public CountryBounds findBounds(String countryCode) {
        for (JsonNode feature : loadFeatures()) {
            String code = feature.path("properties").path("ISO3166-1-Alpha-2").asText();
            if (!countryCode.equalsIgnoreCase(code)) {
                continue;
            }
            double[] bounds = { Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
                    Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY };
            collectBounds(feature.path("geometry").path("coordinates"), bounds);
            return new CountryBounds(bounds[0], bounds[1], bounds[2], bounds[3]);
        }
        return findCatalogFallback(countryCode);
    }

    private CountryBounds findCatalogFallback(String countryCode) {
        for (JsonNode country : loadCatalog()) {
            if (!countryCode.equalsIgnoreCase(country.path("cca2").asText())) {
                continue;
            }
            JsonNode latlng = country.path("latlng");
            if (latlng.isArray() && latlng.size() >= 2) {
                double latitude = latlng.get(0).asDouble();
                double longitude = latlng.get(1).asDouble();
                return new CountryBounds(longitude - 2, latitude - 2, longitude + 2, latitude + 2);
            }
        }
        return null;
    }

    private void collectBounds(JsonNode coordinates, double[] bounds) {
        if (coordinates.isArray() && coordinates.size() >= 2
                && coordinates.get(0).isNumber() && coordinates.get(1).isNumber()) {
            bounds[0] = Math.min(bounds[0], coordinates.get(0).asDouble());
            bounds[1] = Math.min(bounds[1], coordinates.get(1).asDouble());
            bounds[2] = Math.max(bounds[2], coordinates.get(0).asDouble());
            bounds[3] = Math.max(bounds[3], coordinates.get(1).asDouble());
            return;
        }
        for (JsonNode child : coordinates) {
            collectBounds(child, bounds);
        }
    }

    public record CountryBounds(double minLongitude, double minLatitude,
            double maxLongitude, double maxLatitude) {
    }

    private JsonNode loadFeatures() {
        JsonNode loaded = features;
        if (loaded != null) {
            return loaded;
        }
        synchronized (this) {
            if (features == null) {
                String payload = restClient.get().uri(BOUNDARIES_URL).retrieve().body(String.class);
                features = objectMapper.readTree(payload).path("features");
            }
            return features;
        }
    }

    private JsonNode loadCatalog() {
        JsonNode loaded = catalog;
        if (loaded != null) {
            return loaded;
        }
        synchronized (this) {
            if (catalog == null) {
                String payload = restClient.get().uri(COUNTRY_CATALOG_URL).retrieve().body(String.class);
                catalog = objectMapper.readTree(payload);
            }
            return catalog;
        }
    }

    private boolean isInsideGeometry(JsonNode geometry, double longitude, double latitude) {
        String type = geometry.path("type").asText();
        JsonNode polygons = geometry.path("coordinates");
        if ("Polygon".equals(type)) {
            return isInsidePolygon(polygons, longitude, latitude);
        }
        if ("MultiPolygon".equals(type)) {
            for (JsonNode polygon : polygons) {
                if (isInsidePolygon(polygon, longitude, latitude)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isInsidePolygon(JsonNode rings, double longitude, double latitude) {
        if (!rings.isArray() || rings.isEmpty() || !isInsideRing(rings.get(0), longitude, latitude)) {
            return false;
        }
        for (int index = 1; index < rings.size(); index++) {
            if (isInsideRing(rings.get(index), longitude, latitude)) {
                return false;
            }
        }
        return true;
    }

    private boolean isInsideRing(JsonNode ring, double longitude, double latitude) {
        boolean inside = false;
        for (int current = 0, previous = ring.size() - 1; current < ring.size(); previous = current++) {
            double currentLongitude = ring.get(current).get(0).asDouble();
            double currentLatitude = ring.get(current).get(1).asDouble();
            double previousLongitude = ring.get(previous).get(0).asDouble();
            double previousLatitude = ring.get(previous).get(1).asDouble();
            boolean crossesLatitude = (currentLatitude > latitude) != (previousLatitude > latitude);
            if (crossesLatitude) {
                double intersectionLongitude = (previousLongitude - currentLongitude)
                        * (latitude - currentLatitude) / (previousLatitude - currentLatitude) + currentLongitude;
                if (longitude < intersectionLongitude) {
                    inside = !inside;
                }
            }
        }
        return inside;
    }
}
