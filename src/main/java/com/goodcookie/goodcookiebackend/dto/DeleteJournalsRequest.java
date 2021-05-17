package com.goodcookie.goodcookiebackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * TODO this comment
 * Captures the incoming information
 * to delete multiple journals
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeleteJournalsRequest {
    @JsonProperty("journalUrl")
    String journalUrl;
    @JsonProperty("journalCreatedDate")
    Instant journalCreatedDate;
}
