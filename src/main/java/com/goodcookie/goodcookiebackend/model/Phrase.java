package com.goodcookie.goodcookiebackend.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Represents a phrase obtained from the phrase API
 */
public class Phrase {
    @JsonProperty("text")
    private String content;
    @JsonProperty("author")
    private String author;
}
