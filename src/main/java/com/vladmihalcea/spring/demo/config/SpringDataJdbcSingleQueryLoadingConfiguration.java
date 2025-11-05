package com.vladmihalcea.spring.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.mapping.JdbcMappingContext;

/**
 *
 * @author Vlad Mihalcea
 */
@Configuration
public class SpringDataJdbcSingleQueryLoadingConfiguration {

    @Bean
    public JdbcMappingContext jdbcMappingContext() {
        JdbcMappingContext jdbcMappingContext = new JdbcMappingContext();
        jdbcMappingContext.setSingleQueryLoadingEnabled(true);
        return jdbcMappingContext;
    }
}
