package com.goodcookie.goodcookiebackend.security;

import com.goodcookie.goodcookiebackend.config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration class to enable Cross-Origin Resource Sharing requests from front-end
 * application
 */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    private final AppConfig appConfig;

    @Autowired
    public WebConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //TODO: Allow only requests to the api endpoint
        registry.addMapping("/api/**")
                //TODO:Permit only request from the front-end app origin
//                .allowedOriginPatterns("http://localhost:4200", "http://localhost:4200/**")
                .allowedOriginPatterns(this.appConfig.getFrontendUrl(),
                        this.appConfig.getFrontendUrl() + "/**")
                //Permit all http methods
                .allowedMethods("*")
                .maxAge(3600L)
                //Allow all headers
                .allowedHeaders("*")
                .exposedHeaders("Authorization")
                .allowCredentials(true);
    }
}
