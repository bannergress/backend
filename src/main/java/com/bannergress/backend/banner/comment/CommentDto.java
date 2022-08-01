package com.bannergress.backend.banner.comment;

import com.bannergress.backend.agent.NamedAgentDto;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import java.time.Instant;
import java.util.UUID;

/**
 * @param uuid                Internal ID without further meaning.
 * @param comment             Text of the comment.
 * @param author              User who created the comment.
 * @param created             Timestamp the comment was created.
 * @param ratingRoundTheClock Always (24/7) accessible?
 * @param ratingOverall       Overall rating (1 to 5 stars).
 * @param ratingAccessibility Accessibility rating (1 to 5 stars).
 * @param ratingPassphrases   Passphrase rating (1 to 5 stars).
 */
@JsonInclude(Include.NON_NULL)
record CommentDto(@JsonProperty(access = Access.READ_ONLY) UUID uuid, @NotEmpty String comment,
    @JsonProperty(access = Access.READ_ONLY) NamedAgentDto author,
    @JsonProperty(access = Access.READ_ONLY) Instant created, RoundTheClockType ratingRoundTheClock,
    @Min(1) @Max(5) Integer ratingOverall, @Min(1) @Max(5) Integer ratingAccessibility,
    @Min(1) @Max(5) Integer ratingPassphrases) {
}
