package com.goodcookie.goodcookiebackend.controller;

import com.goodcookie.goodcookiebackend.config.AppConfig;
import com.goodcookie.goodcookiebackend.dto.*;
import com.goodcookie.goodcookiebackend.model.NotificationEmail;
import com.goodcookie.goodcookiebackend.model.PasswordResetToken;
import com.goodcookie.goodcookiebackend.model.User;
import com.goodcookie.goodcookiebackend.repository.UserRepository;
import com.goodcookie.goodcookiebackend.service.AuthenticationService;
import com.goodcookie.goodcookiebackend.service.MailService;
import com.goodcookie.goodcookiebackend.service.PasswordResetTokenService;
import com.goodcookie.goodcookiebackend.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth/")
/**
 * Captures all authentication requests
 * for processing with service.
 */
public class AuthenticationController {


    private final AuthenticationService authenticationService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final UserRepository userRepository;
    private final AppConfig appConfig;
    private final MailService mailService;

    @Autowired
    public AuthenticationController(AuthenticationService authenticationService, RefreshTokenService refreshTokenService, PasswordResetTokenService passwordResetTokenService, UserRepository userRepository, AppConfig appConfig, MailService mailService) {
        this.authenticationService = authenticationService;
        this.refreshTokenService = refreshTokenService;
        this.passwordResetTokenService = passwordResetTokenService;
        this.userRepository = userRepository;
        this.appConfig = appConfig;
        this.mailService = mailService;
    }

    /**
     * Captures the incoming user information into a UserDto
     * object and calls on the authentication service to register
     * user into our DB.
     * @param userDto object containing user information for registering
     * @return ResponseEntity indicating the result of the register operation by service.
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserDto userDto){
        return this.authenticationService.registerUser(userDto);
    }


    /**
     * Captures the incoming login user information into an AuthenticationRequest
     * object and calls on the authentication service to verify and authenticate the user.
     * @param authenticationRequest object containing the user login information
     * @return ResponseEntity indicating the result of the login operation by service.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest authenticationRequest){
        return ResponseEntity.ok(this.authenticationService.login(authenticationRequest));
    }

    /**
     * Captures the incoming user and refresh token information into a RefreshTokenRequest object and calls
     * on the authentication service to verify and generate a new jwt for user.
     * @param refreshTokenRequest object containing the information necessary to generate a new token for the user
     * @return AuthenticationResponse object containing updated jwt for user.
     */
    @PostMapping("/refresh/token")
    public AuthenticationResponse refreshTokens(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest){
        return authenticationService.refreshToken(refreshTokenRequest);
    }

    /**
     * Captures the incoming user and refresh token information into a RefreshTokenRequest object
     * and calls on the refresh token service to delete this token from the database.
     * @param refreshTokenRequest object containing information necessary to perform a logout
     * @return ResponseEntity indicating the result of the deleteRefreshToken operation by service.
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody RefreshTokenRequest refreshTokenRequest){
        return this.refreshTokenService.deleteRefreshToken(refreshTokenRequest.getRefreshToken());
    }

    /**
     * Captures incoming forgot password request information
     * to validate user information, generate and issue a PasswordRequestToken
     * @param forgotPasswordRequest the incoming request containing the user email
     * @return A response entity indicating the result of the forgotPassword operation
     */
    @PostMapping("/forgot")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest){
        //Verify there's an user associated with the email
        Optional<User> userOptional = this.userRepository.findByEmail(forgotPasswordRequest.getEmail());

        if(userOptional.isEmpty()) {
            //Didn't find user
            return new ResponseEntity<>("No user associated with that email found.", HttpStatus.BAD_REQUEST);
        }
        //Generate token for user
        PasswordResetToken passwordResetToken = this.passwordResetTokenService.generatePasswordResetToken(userOptional.get());
        //Send email
        mailService.sendMail(
                NotificationEmail.builder()
                        .recipient(userOptional.get().getEmail())
                        .username(userOptional.get().getUsername())
                        .token(passwordResetToken.getToken())
                        .subject("Password Reset")
                        .endpoint(this.appConfig.getFrontendUrl()+"/resetPassword")
                        .build()
        );
        return new ResponseEntity<>("Email Sent.", HttpStatus.OK);
    }

    /**
     * Captures the token incoming on the request, calls on service
     * to validate password reset token.
     * @param token The password reset token to validate
     * @return A ResponseEntity indicating the result of the validatePasswordToken operation by service.
     */
    @PostMapping("/validatePasswordToken/{token}")
    public ResponseEntity<String> validatePasswordToken(@PathVariable("token") String token){
        if(this.passwordResetTokenService.validatePasswordResetToken(token)){
            return ResponseEntity.ok("Valid Token.");
        }
        //Delete the token since it's expired
        this.passwordResetTokenService.deletePasswordResetToken(token);
        return new ResponseEntity<>("Token Expired.", HttpStatus.BAD_REQUEST);
    }

    /**
     * Captures the incoming UpdatePasswordRequest and calls
     * on service to perform password update for an user.
     * @param updatePasswordRequest Request containing the new password and the password reset token
     * @return A ResponseEntity indicating the result of the updatePassword operation by service.
     */
    @PutMapping("/updatePassword")
    public ResponseEntity<String> updatePassword(@RequestBody UpdatePasswordRequest updatePasswordRequest){
        //Get the user email from the token
        User user = this.passwordResetTokenService.getUserFromPasswordResetToken(updatePasswordRequest.getToken());

        //Set the new password of the user
        this.authenticationService.updateUserPassword(user, updatePasswordRequest.getNewPassword());

        //Delete the password reset token
        this.passwordResetTokenService.deletePasswordResetToken(updatePasswordRequest.getToken());

        return new ResponseEntity<>("Password Updated Successfully.", HttpStatus.OK);
    }
}
