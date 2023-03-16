package com.ds.models;

public class FileDownloadData {
    private String fileId;
    private String chunkToWorkerMap;

    public String getChunkToWorkerMap() {
        return this.chunkToWorkerMap;
    }

    public void setChunkToWorkerMap(String chunkToWorkerMap1) {
        this.chunkToWorkerMap = chunkToWorkerMap1;
    }

    public String getFileId() { return this.fileId; }

    public void setFileId(String fileId) { this.fileId = fileId; }
}
