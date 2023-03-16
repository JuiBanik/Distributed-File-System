package com.ds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class ApplicationHome {
    private static final String METADATA_SERVER = "http://3.137.222.232";

    public static void main(String[] args) throws URISyntaxException {
        // Args - port, documentRoot, maxDiskSpace
        SpringApplication app1 = new SpringApplication(ApplicationHome.class);
        Map<String, Object> properties1 = new HashMap<>();
        properties1.put("server.port", "8083");
        properties1.put("local.root", "/Users/jui/Downloads/test/8083/");
        //properties1.put("local.root", "/home/ec2-user/");
        app1.setDefaultProperties(properties1);
        app1.run(args);
        //registerWithMetadataService("http://localhost:8083", 50000000000l);


        SpringApplication app2 = new SpringApplication(ApplicationHome.class);
        Map<String, Object> properties2 = new HashMap<>();
        properties2.put("server.port", "8084");
        properties2.put("local.root", "/Users/jui/Downloads/test/8084/");
        app2.setDefaultProperties(properties2);
        app2.run(args);

        SpringApplication app3 = new SpringApplication(ApplicationHome.class);
        Map<String, Object> properties3 = new HashMap<>();
        properties3.put("server.port", "8085");
        properties3.put("local.root", "/Users/jui/Downloads/test/8085/");
        app3.setDefaultProperties(properties3);
        app3.run(args);

    }

    private static void registerWithMetadataService(String nodeAddress, long availableSpace) throws URISyntaxException {
        //Implementation - call MetadataService with port, address, diskSpace
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
        map.add("nodeId", nodeAddress);
        map.add("availableSpace", availableSpace);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.postForObject(new URI(METADATA_SERVER + "/addWorkerNode"), map, String.class);
    }

}

