package com.goodcookie.goodcookiebackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/**
 * Represents the token sent to the user
 * when a password reset request is made
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "PasswordResetToken")
public class PasswordResetToken {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long Id;

   @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
   @JoinColumn(nullable = false, name = "user_id")
   private User user; //The user to which this unique token has been issued

   @Column(unique = true)
   private String token;

   private Date expiryDate;

}
