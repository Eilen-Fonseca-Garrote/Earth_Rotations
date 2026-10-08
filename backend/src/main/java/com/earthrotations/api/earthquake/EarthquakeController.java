package com.earthrotations.api.earthquake;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/earthquakes")
@CrossOrigin(origins = "*")
public class EarthquakeController {
    private final EarthquakeRepository repository;
    private final EarthquakeImportService importService;

    public EarthquakeController(EarthquakeRepository repository, EarthquakeImportService importService) {
        this.repository = repository;
        this.importService = importService;
    }

    @GetMapping
    public List<Earthquake> findRecent(
            @RequestParam(required = false) String country) {
        Instant since = Instant.now().minus(24, ChronoUnit.HOURS);

        if (country == null || country.isBlank()) {
            return repository.findByOccurredAtGreaterThanEqualOrderByOccurredAtDesc(since);
        }

        return repository.findByCountryAndOccurredAtGreaterThanEqualOrderByOccurredAtDesc(country, since);
    }

    @PostMapping("/sync")
    public SyncResult sync() {
        return new SyncResult(importService.importLatest());
    }

    public record SyncResult(int imported) {
    }
}
