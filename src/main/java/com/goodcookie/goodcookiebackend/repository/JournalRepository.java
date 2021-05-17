package com.goodcookie.goodcookiebackend.repository;

import com.goodcookie.goodcookiebackend.model.Journal;
import com.goodcookie.goodcookiebackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Provides the necessary methods for spring to interact
 * and perform CRUD operations on the Journal table
 * in our database
 */
public interface JournalRepository extends JpaRepository<Journal, Integer> {
    void deleteByJournalURL(String url);
    List<Journal> findAllByUserEquals(User user);
    void deleteAllByJournalURLIsIn(List<String> journalUrlList);
}
