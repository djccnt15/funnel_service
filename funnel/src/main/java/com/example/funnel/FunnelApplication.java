package com.example.funnel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FunnelApplication {

    public static void main(String[] args) {
        SpringApplication.run(FunnelApplication.class, args);
    }

}
