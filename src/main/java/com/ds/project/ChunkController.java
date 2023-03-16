package com.ds.project;

import com.ds.models.ChunkFileWithReplicaData;
import com.ds.models.ChunkMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@RestController
public class ChunkController {
    @Value("${local.root}")
    private String localRoot;

    @PostMapping(value = "/chunk",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> saveDocument(@ModelAttribute ChunkFileWithReplicaData chunkFileWithReplicaData) throws IOException, URISyntaxException {
        System.out.println(chunkFileWithReplicaData.getListOfWorkers());
        System.out.println(chunkFileWithReplicaData.getChunkId());

        saveDocumentToLocal(chunkFileWithReplicaData);
        List <String> workersList = chunkFileWithReplicaData.getListOfWorkers();
        workersList.remove(0); // remove self

        if (workersList.size() != 0) {
            String workerAddress = workersList.get(0);
            URI uri = new URI(workerAddress + "/chunk");

            //call workerAddress with chunkFile and edited list
            MultiValueMap<String, Object> map = getChunkInputMap(chunkFileWithReplicaData, workersList);
            RestTemplate restTemplate = new RestTemplate();
            String result = restTemplate.postForObject(uri, map, String.class);
            System.out.println(result);
        }

        return ResponseEntity.ok("File stored locally in this worker server successfully.");
    }

    @PostMapping(value = "/deletechunk",
            consumes = { MediaType.MULTIPART_FORM_DATA_VALUE,
                    MediaType.APPLICATION_JSON_VALUE,
                    MediaType.APPLICATION_FORM_URLENCODED_VALUE },
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<String> deleteDocument(@ModelAttribute ChunkMetadata chunkMetadata) throws URISyntaxException {
        deleteDocumentFromLocal(chunkMetadata);
        List <String> workersList = chunkMetadata.getListOfWorkers();
        workersList.remove(0); // remove self
        if (workersList.size() != 0) {
            String workerAddress = workersList.get(0);
            //ChunkMetadata replicaMetadata = new ChunkMetadata(chunkMetadata.getChunkId(), workersList);
            MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
            map.add("chunkId", chunkMetadata.getChunkId());
            for (String worker : workersList) {
                map.add("listOfWorkers", worker);
            }
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.postForObject(new URI(workerAddress + "/deletechunk"), map, String.class);
            System.out.println(response);
        }
        return ResponseEntity.ok("File deleted successfully from this worker");
    }

    @GetMapping(value = "/chunk",
            produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE}
    )
    @ResponseBody
    public ResponseEntity<Resource> getChunk(@RequestParam(value = "chunkId") String chunkId) {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("content-disposition",
                String.format("attachment;filename=%s", chunkId));
        responseHeaders.add("Content-Type",MediaType.APPLICATION_OCTET_STREAM_VALUE);

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(new FileSystemResource(Paths.get(localRoot + chunkId)));
    }

    private MultiValueMap<String, Object> getChunkInputMap(ChunkFileWithReplicaData chunkFileWithReplicaData, List<String> workersList) throws IOException {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        final String filename = chunkFileWithReplicaData.getChunkedFile().getOriginalFilename();
        ByteArrayResource contentsAsResource = new ByteArrayResource(
                chunkFileWithReplicaData.getChunkedFile().getBytes()) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
        map.add("chunkedFile", contentsAsResource);
        map.add("chunkId", chunkFileWithReplicaData.getChunkId());
        for (String worker : workersList) {
            map.add("listOfWorkers", worker);
        }
        return map;
    }

    private void saveDocumentToLocal(ChunkFileWithReplicaData chunkFileWithReplicaData) {
        try {
            Files.createDirectories(Paths.get(localRoot));
            MultipartFile file = chunkFileWithReplicaData.getChunkedFile();
            Files.copy(file.getInputStream(),
                    Paths.get(localRoot + chunkFileWithReplicaData.getChunkId()),
                    StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Saved file locally at: "+ chunkFileWithReplicaData.getListOfWorkers().get(0));
        } catch (IOException e) {
            //handle error scenario
            e.printStackTrace();
        }
    }

    private void deleteDocumentFromLocal(ChunkMetadata chunkMetadata) {
        try {
            Files.delete(Paths.get(localRoot
                    + chunkMetadata.getChunkId()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
