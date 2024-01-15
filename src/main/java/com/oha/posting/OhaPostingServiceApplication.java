package com.oha.posting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class OhaPostingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OhaPostingServiceApplication.class, args);
    }

}
