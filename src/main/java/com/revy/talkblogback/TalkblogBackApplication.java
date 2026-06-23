package com.revy.talkblogback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TalkblogBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(TalkblogBackApplication.class, args);
    }
}