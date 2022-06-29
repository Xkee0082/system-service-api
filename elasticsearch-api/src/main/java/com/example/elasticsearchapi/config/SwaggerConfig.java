package com.example.elasticsearchapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * @Author: xu_ke
 * @Date: 2022-06-27 20:15
 * @description TODO
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .title("基于ES的基础接口")
                        .description("提供一个基于ES存储的，包括增删改查的接口API")
                        .contact(new Contact("ES测试", "www.es-api.com", "es_api@163.com"))
                        .version("1.0")
                        .build())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example.elasticsearchapi.controller"))
                .build();
    }
}
