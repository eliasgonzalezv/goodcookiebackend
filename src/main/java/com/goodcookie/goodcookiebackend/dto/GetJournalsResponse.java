package com.goodcookie.goodcookiebackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Captures the information
 * to be returned to client
 * as a response of getJournals request
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetJournalsResponse {
    String journalUrl;
    Date journalCreatedDate;
}
