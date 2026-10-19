package com.earthrotations.api.earthquake;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EarthquakeControllerTest {
    @Mock
    private EarthquakeRepository repository;

    @Mock
    private EarthquakeImportService importService;

    @InjectMocks
    private EarthquakeController controller;

    @Test
    void findRecentWithCountryQueriesThatCountry() {
        List<Earthquake> expected = List.of();
        when(repository.findByCountryAndOccurredAtGreaterThanEqualOrderByOccurredAtDesc(eq("CO"), any(Instant.class)))
                .thenReturn(expected);

        List<Earthquake> actual = controller.findRecent("CO");

        assertSame(expected, actual);
        verify(repository).findByCountryAndOccurredAtGreaterThanEqualOrderByOccurredAtDesc(eq("CO"), any(Instant.class));
    }

    @Test
    void findRecentWithoutCountryQueriesAllRecentEarthquakes() {
        List<Earthquake> expected = List.of();
        when(repository.findByOccurredAtGreaterThanEqualOrderByOccurredAtDesc(any(Instant.class)))
                .thenReturn(expected);

        List<Earthquake> actual = controller.findRecent(null);

        assertSame(expected, actual);
        verify(repository).findByOccurredAtGreaterThanEqualOrderByOccurredAtDesc(any(Instant.class));
    }
}