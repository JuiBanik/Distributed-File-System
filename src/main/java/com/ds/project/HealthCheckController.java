package com.ds.project;

import com.ds.models.HealthStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
    @GetMapping("/health")
    public HealthStatus isHealthy(){
        //when I get a ping from metadata service then I send an acknowledgement that I am healthy (true)
        HealthStatus myStatus = new HealthStatus();
        myStatus.healthStatus = true;
        return myStatus;
    }
}
