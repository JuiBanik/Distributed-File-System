package com.ds;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class ApplicationHome {
    private static final String METADATA_SERVER = "http://3.137.222.232";

    public static void main(String[] args) throws URISyntaxException {
        // Args - port, documentRoot, maxDiskSpace
        SpringApplication app1 = new SpringApplication(ApplicationHome.class);
        Map<String, Object> properties1 = new HashMap<>();
        properties1.put("server.port", "80");

        //Uncomment for local testing
        //properties1.put("local.root", "/Users/jui/Downloads/test/8083/");

        //Comment out for local testing
        properties1.put("local.root", "/home/ec2-user/");

        app1.setDefaultProperties(properties1);
        app1.run(args);
        if (args.length == 2) {
            System.out.println(String.format("Going to register with nodeId: %s", args[1]));
            registerWithMetadataService(args[1], 30000000000l);
        } else {
            System.out.println("Valid nodeId not passed, skipping metadata registration");
        }

        //Uncomment to launch more workers locally for local testing

        /**
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
         **/

    }

    private static void registerWithMetadataService(String nodeAddress, long availableSpace) throws URISyntaxException {
        //Implementation - call MetadataService with port, address, diskSpace
        try {
            MultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();
            map.add("nodeId", nodeAddress);
            map.add("availableSpace", availableSpace);
            RestTemplate restTemplate = new RestTemplate();
            restTemplate.postForObject(new URI(METADATA_SERVER + "/metadata/addWorkerNode"), map, String.class);
            System.out.println("Registered with Metadata server");
        } catch (Exception ex) {
            System.out.println("Failed to register with metadata server");
            ex.printStackTrace();
        }
    }

}

