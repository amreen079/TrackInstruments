package org.track.instruments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class PriceBatchUploadService {
    private final List<PriceDto> inMemoryStore = new CopyOnWriteArrayList<>();
    private static final int DEFAULT_CHUNK_SIZE = 10;
    private boolean batchInProgress = false;


    public List<PriceDto> getAllPrices() {
        return inMemoryStore;
    }


    public void uploadPriceBatch(List<PriceDto> priceBatch) {
        uploadPriceBatch(priceBatch, DEFAULT_CHUNK_SIZE);
    }

    protected void uploadPriceBatch(List<PriceDto> priceBatch, int chunkSize) {
        if (!startBatch()) {
            throw new IllegalStateException("Failed to start batch upload");
        }
        if (priceBatch == null) {
            completeBatch();
            throw new IllegalArgumentException("Price batch cannot be null");
        }
        List<List<PriceDto>> chunks = new ArrayList<>();
        for (int i = 0; i < priceBatch.size(); i += chunkSize) {
            chunks.add(priceBatch.subList(i, Math.min(i + chunkSize, priceBatch.size())));
        }
        List<PriceDto> priceDtoList = Collections.synchronizedList(new ArrayList<>());
        try {
            chunks.parallelStream().forEach(chunk -> priceDtoList.addAll(chunk));
            inMemoryStore.addAll(priceDtoList);
        } catch (Exception e) {
            completeBatch();
            throw new IllegalStateException("Batch upload failed: " + e.getMessage(), e);
        } finally {
            completeBatch();
        }
    }

    public synchronized boolean startBatch() {
        if (batchInProgress) {
            throw new IllegalArgumentException("Another batch upload is already in progress");
        }
        batchInProgress = true;
        return batchInProgress;
    }

    private boolean completeBatch() {
        if (!batchInProgress) {
            throw new IllegalStateException("No batch upload in progress");
        }
        batchInProgress = false;
        return batchInProgress;
    }

    public void cancelBatch() {
        if (!batchInProgress) {
            return;
        }
        batchInProgress = false;
    }
}
