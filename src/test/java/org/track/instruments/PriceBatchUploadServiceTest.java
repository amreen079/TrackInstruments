package org.track.instruments;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PriceBatchUploadServiceTest {
    private PriceBatchUploadService service;

    @BeforeEach
    void setUp() {
        service = new PriceBatchUploadService();
    }

    @Test
    void testSuccessfulBatchUpload() {
        List<PriceDto> batch = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            PriceDto dto = Mockito.mock(PriceDto.class);
            Mockito.when(dto.getId()).thenReturn("id" + i);
            Mockito.when(dto.getAsOf()).thenReturn(LocalDateTime.of(2026, 2, 13, 0, 0));
            batch.add(dto);
        }
        service.uploadPriceBatch(batch, 5);
        assertEquals(20, service.getAllPrices().size());
    }

    @Test
    void testNullBatchThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> service.uploadPriceBatch(null, 5));
    }

    @Test
    void testEmptyBatchUpload() {
        List<PriceDto> batch = new ArrayList<>();
        service.uploadPriceBatch(batch, 5);
        assertEquals(0, service.getAllPrices().size());
    }


    @Test
    void testParallelUploadConsistency() {
        List<PriceDto> batch = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            PriceDto dto = Mockito.mock(PriceDto.class);
            Mockito.when(dto.getId()).thenReturn("id" + i);
            Mockito.when(dto.getAsOf()).thenReturn(LocalDateTime.of(2026, 2, 13, 0, 0));
            batch.add(dto);
        }
        service.uploadPriceBatch(batch, 10);
        assertEquals(100, service.getAllPrices().size());
    }
}
