package com.goodcookie.goodcookiebackend.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import com.goodcookie.goodcookiebackend.model.User;

/**
 * Repository interface for User objects. This interface provides the methods
 * necessary for spring to interact with the DB and perform CRUD operations on
 * the User table.
 */
@Repository
public interface UserRepository extends CrudRepository<User, Integer> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
