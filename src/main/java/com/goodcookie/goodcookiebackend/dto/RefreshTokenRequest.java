package com.goodcookie.goodcookiebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

/**
 * Captures the information necessary
 * to perform a refreshToken operation
 * A unique refreshToken is given to the user at the time of login
 * this is used for operation such as refreshToken and logout.
 * Once a logout is performed, the stored token is deleted.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenRequest {
    @NotEmpty
    //Current refreshToken of user
    private String refreshToken;
    //Username to which the refreshToken belongs to
    private String username;
}
