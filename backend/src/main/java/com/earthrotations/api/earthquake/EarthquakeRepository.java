package com.earthrotations.api.earthquake;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface EarthquakeRepository extends MongoRepository<Earthquake, String> {
    List<Earthquake> findByCountryAndOccurredAtGreaterThanEqualOrderByOccurredAtDesc(
            String country,
            Instant occurredAt);

    List<Earthquake> findByOccurredAtGreaterThanEqualOrderByOccurredAtDesc(Instant occurredAt);
}
