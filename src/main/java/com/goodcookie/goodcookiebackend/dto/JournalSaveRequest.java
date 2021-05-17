package com.goodcookie.goodcookiebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
/**
 * Captures the information necessary
 * to perform a saveJournal operation
 */
public class JournalSaveRequest {
    //Username of the journal creator
    String username;
    //Image file of the journal
    // MultipartFile journalImageFile;
    String journalDataUrl;
}
