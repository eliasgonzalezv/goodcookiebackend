package com.goodcookie.goodcookiebackend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import javax.persistence.*;
import java.time.Instant;

/**
 * TODO implement this class ---- DONE
 * TODO provide a repository for this class --DONE
 * TODO provide journal service
 * TODO provide DTO for journal for saving and deleting journals?
 *
 */

/**
 * Journal model class. This class represents the Journal table
 * in our database.
 */
@Data
@Entity
@Table(name = "Journal")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Journal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer journalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private User user;

    @Column(unique = true, nullable = false)
    private String journalURL;

    @Column(nullable = false)
    private Instant createdDate;

}
