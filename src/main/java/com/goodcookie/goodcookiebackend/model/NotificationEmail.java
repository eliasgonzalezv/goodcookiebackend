package com.goodcookie.goodcookiebackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Encapsulates all the necessary information
 * to be able to send a notification email to user
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationEmail {
    private String subject;
    private String recipient;
    private String username;
    private String endpoint;
    private String token;
}
