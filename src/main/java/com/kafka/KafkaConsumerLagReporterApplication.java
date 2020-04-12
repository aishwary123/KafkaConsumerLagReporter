package com.kafka;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.kafka.configs.CommonConfig;
import com.kafka.services.LagCollector;

@SpringBootApplication
public class KafkaConsumerLagReporterApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(KafkaConsumerLagReporterApplication.class);
    }

    @Override
    public void run(String... args)
        throws Exception {
        CommonConfig.loadConfiguration();
        new LagCollector().startDataCollection();
    }
}
