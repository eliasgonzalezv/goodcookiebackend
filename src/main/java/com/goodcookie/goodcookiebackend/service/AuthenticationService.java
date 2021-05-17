package com.goodcookie.goodcookiebackend.service;

import com.goodcookie.goodcookiebackend.dto.AuthenticationRequest;
import com.goodcookie.goodcookiebackend.dto.AuthenticationResponse;
import com.goodcookie.goodcookiebackend.dto.RefreshTokenRequest;
import com.goodcookie.goodcookiebackend.dto.UserDto;
import com.goodcookie.goodcookiebackend.exception.GoodCookieBackendException;
import com.goodcookie.goodcookiebackend.jwt.JwtUtil;
import com.goodcookie.goodcookiebackend.model.User;
import com.goodcookie.goodcookiebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;

/**
 * Provides logic necessary to handle
 * all authentication requests
 */
@Service
public class AuthenticationService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public AuthenticationService(PasswordEncoder passwordEncoder, UserRepository userRepository, AuthenticationManager authenticationManager, JwtUtil jwtUtil, RefreshTokenService refreshTokenService) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }


    /**
     * Provides the necessary logic to validate the user
     * credential against the database and save a new user.
     * @param userDto Instance containing user information
     * @return A ResponseEntity object denoting the status of the registration
     */
    @Transactional
    public ResponseEntity<String> registerUser(UserDto userDto){
        //Create User object to hold values from UserDto
        User newUser = new User();
        newUser.setUsername(userDto.getUsername());
        newUser.setEmail(userDto.getEmail());
        newUser.setPassword(passwordEncoder.encode(userDto.getPassword()));
        //Holds registration message
        String message;
        //Holds response http status code
        HttpStatus statusCode;
        //Does the username provided already exist?
        if(userRepository.existsByUsername(newUser.getUsername())){
            message = "Username already exists.";
            statusCode = HttpStatus.CONFLICT;
        }
        //Does the email provided already exist?
        else if(userRepository.existsByEmail(newUser.getEmail())){
            message = "Email already in use.";
            statusCode = HttpStatus.CONFLICT;
        }
        else{
            //Save user
            userRepository.save(newUser);
            message = "User registration successful.";
            statusCode = HttpStatus.OK;
        }
        return new ResponseEntity<>(message, statusCode);
    }

    /**
     * Provides the necessary logic to login an user
     */
    public AuthenticationResponse login(AuthenticationRequest authenticationRequest){

        try {
            //Authenticate user
            Authentication authentication = authenticationManager.authenticate(new
                    UsernamePasswordAuthenticationToken(
                    authenticationRequest.getUsername(),
                    authenticationRequest.getPassword()
            ));
            //Set authentication
            SecurityContextHolder.getContext().setAuthentication(authentication);

            //Generate token
            String token = jwtUtil.generateToken(authentication);

            //Return the token to user
            return AuthenticationResponse.builder()
                    .authenticationToken(token)
                    .refreshToken(refreshTokenService.generateRefreshToken().getToken())
                    .expiresAt(Instant.now().plusMillis(jwtUtil.getJwtExpirationInMillis()))
                    .username(authenticationRequest.getUsername())
                    .build();

        } catch(BadCredentialsException e) {
            throw new GoodCookieBackendException("Incorrect username or password", e);
        }

    }

    /**
     * Returns a newly generated refresh token for the user
     * provided in the refresh token request
     * @param refreshTokenRequest Incoming request from frontend
     * @return AuthenticationResponse containing the newly generated token
     */
    public AuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        //Is token valid?
        if(refreshTokenService.validateRefreshToken(refreshTokenRequest.getRefreshToken())) {
            //Refresh the jwt token
            String token = jwtUtil.generateTokenWithUsername(refreshTokenRequest.getUsername());
            return AuthenticationResponse.builder()
                    .authenticationToken(token)
                    .refreshToken(refreshTokenRequest.getRefreshToken())
                    .expiresAt(Instant.now().plusMillis(jwtUtil.getJwtExpirationInMillis()))
                    .username(refreshTokenRequest.getUsername())
                    .build();
        }else{
            throw new GoodCookieBackendException("Invalid RefreshToken");
        }
    }

    /**
     * Updates the user password
     * @param user The user for which to update the password
     * @param newPassword The new password for the user
     */
    public void updateUserPassword(User user, String newPassword){
       user.setPassword(passwordEncoder.encode(newPassword));
       this.userRepository.save(user);
    }

    /**
     * Checks whether the user is logged in
     * @return true if user is logged in
     */
//    public boolean userIsLoggedIn(){
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        return !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();
//    }
}
