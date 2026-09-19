package com.thnky.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Supabase Postgres, created only when SUPABASE_DB_URL is set.
 * Without it the app runs on the static bank alone, with no database.
 */
@Configuration
@ConditionalOnExpression("!'${thnky.db.url:}'.isEmpty()")
class DatabaseConfig {

    @Bean
    DataSource dataSource(
            @Value("${thnky.db.url}") String url,
            @Value("${thnky.db.user:}") String user,
            @Value("${thnky.db.password:}") String password) {
        return DataSourceBuilder.create().url(url).username(user).password(password).build();
    }
}
