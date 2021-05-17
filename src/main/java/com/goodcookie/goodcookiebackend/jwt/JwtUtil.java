package com.goodcookie.goodcookiebackend.jwt;

import com.goodcookie.goodcookiebackend.exception.GoodCookieBackendException;
import com.goodcookie.goodcookiebackend.security.SecurityConstantsConfig;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;


@Service
public class JwtUtil {

//    @Value("${security.jwt.expiration.time}")
//    private Long jwtExpirationTimeInMillis;

    private final SecurityConstantsConfig securityConstantsConfig;

    @Autowired
    public JwtUtil(SecurityConstantsConfig securityConstantsConfig) {
        this.securityConstantsConfig = securityConstantsConfig;
    }

    /**
     * Extracts the username from the provided Json Web Token
     * @param jwt an income JSON Web Token
     * @return the username associated with the jwt
     */
    public String extractUsername(String jwt){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(securityConstantsConfig.getSecretKey().getBytes()))
                .build()
                .parseClaimsJws(jwt)
                .getBody();
        return claims.getSubject();
    }

    /**
     * Generates and returns a Json Web Token for the specific user
     * @param authentication Authentication Object received from controller
     * @return A JWT token for the user
     */
    public String generateToken(Authentication authentication){
        //Fetch user from the authentication
        org.springframework.security.core.userdetails.User principal = (User) authentication.getPrincipal();

        return Jwts.builder()
                .setSubject(principal.getUsername())
                .setIssuedAt(Date.from(Instant.now()))
                .signWith(Keys.hmacShaKeyFor(securityConstantsConfig.getSecretKey().getBytes()))
                .setExpiration(Date.from(Instant.now().plusMillis(securityConstantsConfig.getExpirationTimeInMillis())))
                .compact();


    }

    /**
     * Generates and returns a token. Used for refresh tokens,
     * specifically in the case that the user token has expired and
     * therefore there's no user information in the security context.
     * @param username Username to associate with the token
     * @return A generated token associated with the username provided
     */
    public String generateTokenWithUsername(String username){

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(Date.from(Instant.now()))
                .signWith(Keys.hmacShaKeyFor(securityConstantsConfig.getSecretKey().getBytes()))
                .setExpiration(Date.from(Instant.now().plusMillis(securityConstantsConfig.getExpirationTimeInMillis())))
                .compact();


    }

    public Long getJwtExpirationInMillis() {
        return this.securityConstantsConfig.getExpirationTimeInMillis();
    }
    /**
     * Validate the given token by decoding it and checking its expiration time
     * @param jwt The jwt to validate
     * @return true if the token is valid
     */
    public boolean validateToken(String jwt){
        try {
            Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(
                            securityConstantsConfig.getSecretKey().getBytes()))
                    .build()
                    .parseClaimsJws(jwt);

            return true;
        }
        catch (SignatureException | MalformedJwtException
                | UnsupportedJwtException | IllegalArgumentException e){
            throw new GoodCookieBackendException("Invalid credentials" , e);
        }
        catch (ExpiredJwtException e){
            throw new GoodCookieBackendException("JWT provided is Expired", e);
        }

    }



}
