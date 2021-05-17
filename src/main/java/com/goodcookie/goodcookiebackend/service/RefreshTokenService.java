package com.goodcookie.goodcookiebackend.service;

import com.goodcookie.goodcookiebackend.exception.GoodCookieBackendException;
import com.goodcookie.goodcookiebackend.model.RefreshToken;
import com.goodcookie.goodcookiebackend.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.UUID;

/**
 * Creates, Deletes and Validates Refresh Tokens
 * Refresh Tokens are sent to user whenever the originally
 * sent tokens are expired or about to expired.
 */
@Service
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Autowired
    public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    /**
     * Generates a new refresh token and saves it
     * to the DB using the refreshTokenRepository
     * @return A generated refresh token
     */
    public RefreshToken generateRefreshToken(){
        RefreshToken refreshToken = new RefreshToken();
        //Creates a 128bit random UUID. This serves as our refresh token
        refreshToken.setToken(UUID.randomUUID().toString());
        //Set creation timestampt
        refreshToken.setCreatedDate(Instant.now());

        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * Looks for the provided token in DB
     * @param token the token to be validated
     */
    public boolean validateRefreshToken(String token){
        return refreshTokenRepository.findByToken(token).isPresent();
    }

    /**
     * Deletes the provided token from the DB
     * @param token the token to be deleted
     */
    public ResponseEntity<String> deleteRefreshToken(String token){
        refreshTokenRepository.deleteByToken(token);
        return new ResponseEntity<>("Refresh Token Deleted Successfully.", HttpStatus.OK);
    }
}
