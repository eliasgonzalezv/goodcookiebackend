package com.goodcookie.goodcookiebackend.security;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * TODO: Move all these constants over to app properties file
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Configuration
public class SecurityConstantsConfig {


    @Value("${security.jwt.secret.value}")
    private String secretKey;

    @Value("${security.jwt.expiration.time}")
    private Long expirationTimeInMillis;

    @Value("${security.jwt.auth.token.prefix}")
    private String tokenPrefix;

    @Value("${security.jwt.auth.header.name}")
    private String headerName;
}
