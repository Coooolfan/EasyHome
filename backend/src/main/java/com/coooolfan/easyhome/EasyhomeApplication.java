package com.coooolfan.easyhome;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class EasyhomeApplication {
    public static void main(String[] args) {
        SpringApplication.run(EasyhomeApplication.class, args);
    }

}
