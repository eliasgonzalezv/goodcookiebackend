package com.goodcookie.goodcookiebackend.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object class for an user. This class helps catch all of the
 * registration information coming from the frontend.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    @NotNull
    @NotEmpty(message = "Username cannot be empty.")
    private String username;

    @NotNull
    @NotEmpty(message = "Password cannot be empty.")
    private String password;

    @NotNull
    @NotEmpty(message = "Email cannot be empty.")
    private String email;

}
