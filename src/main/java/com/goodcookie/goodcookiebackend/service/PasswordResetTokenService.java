package com.goodcookie.goodcookiebackend.service;

import com.goodcookie.goodcookiebackend.exception.GoodCookieBackendException;
import com.goodcookie.goodcookiebackend.model.PasswordResetToken;
import com.goodcookie.goodcookiebackend.model.User;
import com.goodcookie.goodcookiebackend.repository.PasswordResetTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

/**
 * Contains the necessary logic to
 * Generate, Delete and Validate PasswordResetTokens
 * */
@Service
@Transactional
public class PasswordResetTokenService {

   private final PasswordResetTokenRepository passwordResetTokenRepository;

   @Autowired
   public PasswordResetTokenService(PasswordResetTokenRepository passwordResetTokenRepository) {
      this.passwordResetTokenRepository = passwordResetTokenRepository;
   }


   /**
    * Generates and saves PasswordResetToken for the given user
    * @param user The user that requested a password reset
    * @return The generated password reset token
    */
   public PasswordResetToken generatePasswordResetToken(User user){
      //Generate random uuid to use as token
      String token = UUID.randomUUID().toString();

      //Create token to be saved
      PasswordResetToken toSave = PasswordResetToken.builder()
              .token(token)
              .user(user)
              //Set expiry for 30 mins from date created
              .expiryDate(Date.from(Instant.now().plusSeconds(1800)))
              .build();
      //Save the token and return it
      return this.passwordResetTokenRepository.save(toSave);
   }

   /**
    * Verifies if the token provided is valid or not
    * @param token the token to validate
    * @return true if token is valid and not expired
    */
   public boolean validatePasswordResetToken(String token){
      //Attempt to find the token
      PasswordResetToken passwordResetToken=
              this.passwordResetTokenRepository.findByToken(token)
                      .orElseThrow(() -> {throw new GoodCookieBackendException("Invalid Password Reset Token");});

      //Is token expired?
      return !passwordResetToken.getExpiryDate().before(Date.valueOf(LocalDate.now()));
   }

   /**
    * Deletes the password reset token from the DB
    * @param token token to delete
    */
   public void deletePasswordResetToken(String token){
      this.passwordResetTokenRepository.deleteByToken(token);
   }

   /**
    * Retrieves the user information from the token
    */
   public User getUserFromPasswordResetToken(String token){
      return this.passwordResetTokenRepository.findByToken(token).get().getUser();
   }
}
