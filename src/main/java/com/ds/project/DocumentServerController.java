package com.ds.project;

import com.ds.models.FileDownloadData;
import com.ds.models.FileMetadata;
import com.ds.models.ResponseMessage;
import com.ds.util.ChunkUtil;
import com.ds.util.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class DocumentServerController {

    @Value("${local.root}")
    private String localRoot;

    private ObjectMapper objectMapper = new ObjectMapper();

    @PostMapping(value = "/upload",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<ResponseMessage> uploadDocument(@ModelAttribute FileMetadata fileMetadata) {
        System.out.println(fileMetadata.getChunkToWorkerMap());
        try {
            Map<String, List<String>> chunkWorkerMap = objectMapper.readValue(fileMetadata.getChunkToWorkerMap(), Map.class);
            MultipartFile file = fileMetadata.getFile();
            String fileName = localRoot + file.getOriginalFilename();
            Files.createDirectories(Paths.get(localRoot));
            Files.copy(file.getInputStream(), Paths.get(fileName), StandardCopyOption.REPLACE_EXISTING);
            Map<String, String> chunkIdToFileNameMap = ChunkUtil.splitFile(localRoot, fileName,
                    chunkWorkerMap.keySet().stream().toList());
            sendChunksToWorkers(chunkIdToFileNameMap, chunkWorkerMap);

            Files.delete(Paths.get(fileName));
            for (String chunkId: chunkIdToFileNameMap.keySet()) {
                Files.delete(Paths.get(chunkIdToFileNameMap.get(chunkId)));
            }
            ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setStatus("Success");
            responseMessage.setMessage("File Upload Successful");
            return ResponseEntity.ok(responseMessage);
        } catch (Exception e) {
            e.printStackTrace();
            ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setStatus("Failed");
            responseMessage.setMessage("File Upload Failed " + e.getMessage());
            return ResponseEntity.internalServerError().body(responseMessage);
        }
    }

    @PostMapping(value = "/download",
            produces = {MediaType.APPLICATION_OCTET_STREAM_VALUE}
    )
    @ResponseBody
    public FileSystemResource downloadDocument(@ModelAttribute FileDownloadData fileDownloadData) throws Exception {
        Map<String, List<String>> chunkWorkerMap = objectMapper.readValue(
                fileDownloadData.getChunkToWorkerMap(), Map.class);
        String fileId = fileDownloadData.getFileId();
        File[] chunks = getChunksFromWorkers(chunkWorkerMap);
        String mergedFileName = ChunkUtil.joinFiles(chunks, fileId, localRoot);
        FileSystemResource fileSystemResource = new FileSystemResource(Paths.get(mergedFileName));
        //Files.delete(Paths.get(mergedFileName));
        for (File chunk: chunks) {
            chunk.delete();
        }
        return fileSystemResource;
    }

    @PostMapping(value = "/delete")
    public ResponseEntity<ResponseMessage> deleteDocument(@ModelAttribute FileDownloadData fileDownloadData) throws Exception {
        try {
            Map<String, List<String>> chunkWorkerMap = objectMapper.readValue(
                    fileDownloadData.getChunkToWorkerMap(), Map.class);
            String fileId = fileDownloadData.getFileId();
            for (String chunkId : chunkWorkerMap.keySet()) {
                List<String> workers = chunkWorkerMap.get(chunkId);
                String workerId = workers.get(0);
                MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
                map.add("chunkId", chunkId);
                for (String worker : workers) {
                    map.add("listOfWorkers", worker);
                }
                RestTemplate restTemplate = new RestTemplate();
                ResponseMessage response = restTemplate.postForObject(new URI(workerId + "/deletechunk"), map,
                        ResponseMessage.class);
                if (!Constants.SUCCESS.equalsIgnoreCase(response.getStatus())) {
                    throw new RuntimeException("Error deleting chunk. " +  response.getMessage());
                }
            }
            ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setStatus("Success");
            responseMessage.setMessage(String.format("Successfully deleted file with id: %s", fileId));
            return ResponseEntity.ok(responseMessage);
        } catch (Exception ex) {
            ex.printStackTrace();
            ResponseMessage responseMessage = new ResponseMessage();
            responseMessage.setStatus("Failed");
            responseMessage.setMessage("Failed to delete file. " + ex.getMessage());
            return ResponseEntity.internalServerError().body(responseMessage);
        }
    }

    private File[] getChunksFromWorkers(Map<String, List<String>> chunkWorkerMap) throws URISyntaxException, IOException {
        List<File> files = new ArrayList<>();
        File[] fileList = new File[chunkWorkerMap.keySet().size()];
        int i = 0;
        for (String chunkId: chunkWorkerMap.keySet()) {
            String workerId = chunkWorkerMap.get(chunkId).get(0);
            URL website = new URL(workerId + String.format("/chunk?chunkId=%s", chunkId));
            try (InputStream in = website.openStream()) {
                fileList[i] = ChunkUtil.saveFile(localRoot, in, chunkId);
            }
            i++;
        }
        return fileList;
    }

    private void sendChunksToWorkers(Map<String, String> chunkIdToFileNameMap,
                                     Map<String, List<String>> chunkWorkerMap) throws IOException, URISyntaxException {
        for (String chunkId: chunkIdToFileNameMap.keySet()) {
            String chunkFileName = chunkIdToFileNameMap.get(chunkId);
            List<String> workerList = chunkWorkerMap.get(chunkId);

            String workerAddress = workerList.get(0);
            URI uri = new URI(workerAddress + "/chunk");

            MultiValueMap<String, Object> map = getChunkInputMap(chunkId, chunkFileName, workerList);
            RestTemplate restTemplate = new RestTemplate();
            ResponseMessage result = restTemplate.postForObject(uri, map, ResponseMessage.class);
            if (!Constants.SUCCESS.equalsIgnoreCase(result.getStatus())) {
                throw new RuntimeException("Error sending chunk to workers. " + result.getMessage());
            }
        }
    }

    private MultiValueMap<String, Object> getChunkInputMap(String chunkId, String chunkFileName,
                                                           List<String> workersList) throws IOException {
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        byte[] bytes = Files.readAllBytes(Paths.get(chunkFileName));
        ByteArrayResource contentsAsResource = new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return chunkFileName;
            }
        };
        map.add("chunkedFile", contentsAsResource);
        map.add("chunkId", chunkId);
        for (String worker : workersList) {
            map.add("listOfWorkers", worker);
        }
        return map;
    }
}
