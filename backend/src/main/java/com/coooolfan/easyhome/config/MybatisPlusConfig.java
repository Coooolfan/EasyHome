package com.coooolfan.easyhome.config;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author lima
 * @version 0.0.1
 **/
@Configuration
@MapperScan("com.coooolfan.easyhome.mapper")
public class MybatisPlusConfig {
}
