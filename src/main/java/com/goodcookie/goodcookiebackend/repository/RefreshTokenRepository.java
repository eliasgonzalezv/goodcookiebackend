package com.goodcookie.goodcookiebackend.repository;

import com.goodcookie.goodcookiebackend.model.RefreshToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;
/**
 * Provides the methods necessary for spring to
 * interact and perform CRUD operations with the
 * refreshtoken table in the database
 */
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    void deleteByToken(String token);
}
