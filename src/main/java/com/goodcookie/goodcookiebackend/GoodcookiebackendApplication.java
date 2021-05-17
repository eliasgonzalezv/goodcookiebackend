package com.goodcookie.goodcookiebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableConfigurationProperties
//Main class of spring application
public class GoodcookiebackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoodcookiebackendApplication.class, args);
	}

	//Register rest template bean
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder){
		return builder.build();
	}


}
