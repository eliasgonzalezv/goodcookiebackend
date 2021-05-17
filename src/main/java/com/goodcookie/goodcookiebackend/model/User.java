package com.goodcookie.goodcookiebackend.model;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * User model class. This class represents the User table in our database.
 * Spring maps this class automatically into a DB table.
 */
@Entity
@Table(name = "User")
@Data // Generate getters and setters
@AllArgsConstructor // Generate constructor with all args
@NoArgsConstructor // Generate empty constructor
@Builder // Generate builder method for this class
public class User {

    @Id // Specify the primary key of the table
    // Key values generated using identity strategy
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Integer userId;

    @Column(name="username" , unique = true, nullable = false)
    @NotNull
    private String username;

    @Email
    @NotNull
    @Column(unique = true, nullable = false)
    private String email;

    @NotNull
    @Column(nullable = false)
    private String password;
}
