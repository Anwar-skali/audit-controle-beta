package com.audit.platform.finding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.audit.platform.finding.client")
public class FindingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FindingServiceApplication.class, args);
    }
}
