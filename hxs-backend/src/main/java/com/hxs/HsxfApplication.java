package com.hxs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HsxfApplication {

    public static void main(String[] args) {
        SpringApplication.run(HsxfApplication.class, args);
    }

}
