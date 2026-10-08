package com.earthrotations.api.earthquake;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class EarthquakeApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EarthquakeRepository repository;

    @MockitoBean
    private EarthquakeImportService importService;

    @Test
    void getEarthquakesByCountryReturnsJsonArray() throws Exception {
        when(repository.findByCountryAndOccurredAtGreaterThanEqualOrderByOccurredAtDesc(eq("CO"), any(Instant.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/earthquakes").param("country", "CO"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(repository).findByCountryAndOccurredAtGreaterThanEqualOrderByOccurredAtDesc(eq("CO"), any(Instant.class));
    }

    @Test
    void getEarthquakesWithoutCountryReturnsJsonArray() throws Exception {
        when(repository.findByOccurredAtGreaterThanEqualOrderByOccurredAtDesc(any(Instant.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/earthquakes"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        verify(repository).findByOccurredAtGreaterThanEqualOrderByOccurredAtDesc(any(Instant.class));
    }
}