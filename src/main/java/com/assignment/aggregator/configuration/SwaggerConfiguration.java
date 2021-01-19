package com.assignment.aggregator.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Configures a Swagger2 instance providing automatic API documentation
 * <p>
 * The Swagger2 UI may be accessed in the application's root path / swagger-ui.html
 *
 * @see "http://localhost:8080/api/swagger-ui.html"
 */
@Configuration
@EnableSwagger2
public class SwaggerConfiguration
{
    @Bean
    public Docket api()
    {
        return new Docket(DocumentationType.SWAGGER_2)
                       .select()
                       .apis(RequestHandlerSelectors.any())
                       .paths(PathSelectors.any())
                       .build();
    }
}