package com.thnky.api.dto;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * {@code answer} is a raw JSON node because its shape depends on the
 * challenge type: an index for choice/lines, free text for text/code.
 */
public record AnswerRequest(String challengeId, JsonNode answer, int hintsUsed, int seconds) {
}
