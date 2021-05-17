package com.goodcookie.goodcookiebackend.repository;

import com.goodcookie.goodcookiebackend.model.PasswordResetToken;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

/**
 * Provides the methods necessary to perform CRUD
 * operations on the PasswordResetToken table
 */
public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetToken, Long> {
    //Retrieves the information for a PasswordResetToken given the token
    Optional<PasswordResetToken> findByToken(String token);

    void deleteByToken(String token);
}
