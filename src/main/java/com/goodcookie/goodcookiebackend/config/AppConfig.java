package com.goodcookie.goodcookiebackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@Component
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "app.properties")
public class AppConfig {
    @NotNull
    private String url;

    @NotNull
    private String frontendUrl;

    public String getUrl(){
        return url;
    }

    public String getFrontendUrl(){
        return frontendUrl;
    }

    public void setUrl(String url){
        this.url = url;
    }

    public void setFrontendUrl(String frontendUrl){
        this.frontendUrl = frontendUrl;
    }

}
