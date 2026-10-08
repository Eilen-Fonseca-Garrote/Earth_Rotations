package com.earthrotations.api.country;

import tools.jackson.databind.JsonNode;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/countries")
@CrossOrigin(origins = "*")
public class CountryController {
    private static final String COUNTRIES_URL =
            "https://raw.githubusercontent.com/mledoze/countries/master/countries.json";

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper objectMapper;
    private final CountryBoundaryService countryBoundaryService;

    public CountryController(ObjectMapper objectMapper, CountryBoundaryService countryBoundaryService) {
        this.objectMapper = objectMapper;
        this.countryBoundaryService = countryBoundaryService;
    }

    @GetMapping
    public JsonNode findAll() {
        String payload = restClient.get().uri(COUNTRIES_URL).retrieve().body(String.class);
        return objectMapper.readTree(payload);
    }

    @GetMapping("/{code}/bounds")
    public CountryBoundaryService.CountryBounds findBounds(@PathVariable String code) {
        return countryBoundaryService.findBounds(code);
    }
}
