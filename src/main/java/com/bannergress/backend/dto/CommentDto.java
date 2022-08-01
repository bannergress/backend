package com.bannergress.backend.dto;

import com.bannergress.backend.agent.NamedAgentDto;
import com.bannergress.backend.banner.comment.CommentType;
import com.bannergress.backend.banner.comment.RoundTheClockType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(Include.NON_NULL)
public class CommentDto {
    /** Internal ID without further meaning. */
    public UUID uuid;

    /** Text of the comment. */
    public String comment;

    /** Type of comment (comment or review). */
    @NotNull
    public CommentType type;

    /** User who created the comment. */
    public NamedAgentDto author;

    /** Timestamp the comment was created. */
    public Instant created;

    /** Always (24/7) accessible. */
    public RoundTheClockType ratingRoundTheClock;

    /** Overall rating (1 to 5 stars). */
    @Min(1)
    @Max(5)
    public Integer ratingOverall;

    /** Accessibility rating (1 to 5 stars). */
    @Min(1)
    @Max(5)
    public Integer ratingAccessibility;

    /** Passphrase rating (1 to 5 stars). */
    @Min(1)
    @Max(5)
    public Integer ratingPassphrases;
}
