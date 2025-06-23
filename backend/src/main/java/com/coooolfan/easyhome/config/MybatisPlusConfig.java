package com.coooolfan.easyhome.config;


import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.coooolfan.easyhome.handle.StringListTypeHandler;
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
    @Bean
    public ConfigurationCustomizer configurationCustomizer() {
        return configuration -> configuration.getTypeHandlerRegistry().register(StringListTypeHandler.class);
    }
}
