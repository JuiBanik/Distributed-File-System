package com.ds.util;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ChunkUtil {

    private static final String FILE_ROOT = "/Users/jui/Downloads/test/";
    public static final String INPUT_FILE = "/Users/jui/Downloads/hubris.png";
    public static final int NUMBER_OF_OUTPUT_FILES = 3;
    private static final String CHUNK_FOLDER_NAME = "_Chunks/";

    /**
     * split file
     *
     * @throws Exception
     */
    public static Map<String, String> splitFile(String fileRoot, String inputFileName,
                                                List<String> chunkIdList) throws Exception {
        String chunkRoot = fileRoot + CHUNK_FOLDER_NAME;
        int numOutputFiles = chunkIdList.size();
        Map<String, String> chunkIdToFileNameMap = new HashMap<>();
        File inputFile = new File(chunkRoot);
        inputFile.mkdir();

        RandomAccessFile raf = new RandomAccessFile(inputFileName, "r");

        long sourceSize = raf.length();
        long bytesPerSplit = sourceSize / numOutputFiles;
        long remainingBytes = sourceSize % numOutputFiles;

        int maxReadBufferSize = 8 * 1024; // 8KB
        for (int destIx = 1; destIx <= numOutputFiles; destIx++) {
            String chunkFileName = chunkRoot + chunkIdList.get(destIx-1);
            BufferedOutputStream bw = new BufferedOutputStream(
                    new FileOutputStream(chunkFileName));
            if (bytesPerSplit > maxReadBufferSize) {
                long numReads = bytesPerSplit / maxReadBufferSize;
                long numRemainingRead = bytesPerSplit % maxReadBufferSize;
                for (int i = 0; i < numReads; i++) {
                    readWrite(raf, bw, maxReadBufferSize);
                }
                if (numRemainingRead > 0) {
                    readWrite(raf, bw, numRemainingRead);
                }
            } else {
                readWrite(raf, bw, bytesPerSplit);
            }
            bw.close();
            chunkIdToFileNameMap.put(chunkIdList.get(destIx-1), chunkFileName);
        }
        if (remainingBytes > 0) {
            BufferedOutputStream bw = new BufferedOutputStream(new FileOutputStream("split." + numOutputFiles + 1));
            readWrite(raf, bw, remainingBytes);
            bw.close();
        }
        raf.close();
        return chunkIdToFileNameMap;
    }

    public static String joinFiles(File[] files, String documentId, String fileRoot) throws Exception {
        int maxReadBufferSize = 8 * 1024;

        String mergedFileName = fileRoot + documentId;
        BufferedOutputStream bw = new BufferedOutputStream(
                new FileOutputStream(mergedFileName));

        RandomAccessFile raf = null;
        for (File file : files) {
            raf = new RandomAccessFile(file, "r");
            long numReads = raf.length() / maxReadBufferSize;
            long numRemainingRead = raf.length() % maxReadBufferSize;
            for (int i = 0; i < numReads; i++) {
                readWrite(raf, bw, maxReadBufferSize);
            }
            if (numRemainingRead > 0) {
                readWrite(raf, bw, numRemainingRead);
            }
            raf.close();

        }
        bw.close();
        return mergedFileName;
    }

    public static File saveFile (String localRoot, InputStream inputStream, String fileId) throws IOException {
        String fileName = localRoot + CHUNK_FOLDER_NAME + fileId;
        Files.copy(inputStream, Paths.get(fileName),
                StandardCopyOption.REPLACE_EXISTING);
        return new File(fileName);
    }

    public static String joinFiles(String fileRoot, String inputFileName, List<String> chunkIdList) throws Exception {
        int numOutputFiles = chunkIdList.size();
        String chunkRoot = fileRoot + CHUNK_FOLDER_NAME;
        File[] files = new File[numOutputFiles];
        for (int i = 1; i <= numOutputFiles; i++) {
            files[i - 1] = new File(chunkRoot  + chunkIdList.get(i-1));
        }
        return joinFiles(files, inputFileName, fileRoot);
    }

    public static void main(String[] args) throws Exception {
        List<String> chunkIdList = new ArrayList<>();
        chunkIdList.add("C1");
        chunkIdList.add("C2");
        chunkIdList.add("C3");
        Map<String, String> chunkFileNames = splitFile(FILE_ROOT, INPUT_FILE, chunkIdList);
        System.out.println(chunkFileNames);
        String mergedFileName = joinFiles(FILE_ROOT, "hubris.png", chunkIdList);
        System.out.println(mergedFileName);
    }

    static void readWrite(RandomAccessFile raf, BufferedOutputStream bw, long numBytes) throws IOException {
        byte[] buf = new byte[(int) numBytes];
        int val = raf.read(buf);
        if (val != -1) {
            bw.write(buf);
        }
    }
}
