package com.ds.models;

import java.util.List;

public class ChunkMetadata {
    private String chunkId;
    private List<String> listOfWorkers;

    public ChunkMetadata(String chunkId, List<String> listOfWorkers) {
        this.chunkId = chunkId;
        this.listOfWorkers = listOfWorkers;
    }

    public String getChunkId() {
        return this.chunkId;
    }

    public List<String> getListOfWorkers() {
        return this.listOfWorkers;
    }
}
