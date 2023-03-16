package com.ds.models;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class ChunkFileWithReplicaData {
    private MultipartFile chunkedFile;
    private List<String> listOfWorkers;
    public String chunkId;
    //public List<String> workerId;


    public List<String> getListOfWorkers() {
        return listOfWorkers;
    }

    public void setListOfWorkers(List<String> workerList) {
        listOfWorkers = workerList;
    }

    public String getChunkId() {
        return chunkId;
    }

    public void setChunkId(String chunkId) {
        this.chunkId = chunkId;
    }

    public MultipartFile getChunkedFile() {
        return this.chunkedFile;
    }

    public void setChunkedFile(MultipartFile file) {
        this.chunkedFile = file;
    }
}
