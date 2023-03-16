package com.ds.models;

import org.springframework.web.multipart.MultipartFile;

public class FileMetadata {
    private MultipartFile file;
    private String chunkToWorkerMap;
    private long chunkSize;

    public String getChunkToWorkerMap() {
        return this.chunkToWorkerMap;
    }

    public void setChunkToWorkerMap(String chunkToWorkerMap1) {
        this.chunkToWorkerMap = chunkToWorkerMap1;
    }

    public MultipartFile getFile() {
        return this.file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public long getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(long chunkSize) {
        this.chunkSize = chunkSize;
    }
}
