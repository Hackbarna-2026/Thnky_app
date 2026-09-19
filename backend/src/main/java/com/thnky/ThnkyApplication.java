package com.thnky;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

// The datasource is wired by DatabaseConfig, only when Supabase is configured.
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class ThnkyApplication {

    public static void main(String[] args) {
        SpringApplication.run(ThnkyApplication.class, args);
    }
}
