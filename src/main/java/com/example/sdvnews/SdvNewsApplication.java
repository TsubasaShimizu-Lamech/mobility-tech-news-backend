package com.example.sdvnews;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SdvNewsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SdvNewsApplication.class, args);
    }
}
