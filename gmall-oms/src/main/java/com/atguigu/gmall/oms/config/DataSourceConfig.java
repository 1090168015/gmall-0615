package com.atguigu.gmall.oms.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

public class DataSourceConfig {


    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSource dataSource(@Value("{spring.datasource.url}") String url){
        HikariDataSource hikariDataSource =new HikariDataSource();
        hikariDataSource.setJdbcUrl(url);
        return  hikariDataSource;
    }
}