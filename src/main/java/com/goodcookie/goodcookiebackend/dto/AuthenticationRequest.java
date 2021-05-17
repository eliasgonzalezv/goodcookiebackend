package com.goodcookie.goodcookiebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object class for an Authentication Request.
 * An instance of this class holds the information
 * provided by an user who's attempting to login to the system.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthenticationRequest {
    private String username;
    private String password;
}
