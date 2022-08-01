package com.bannergress.backend.services;

import com.bannergress.backend.dto.CommentDto;
import com.bannergress.backend.entities.Banner;
import com.bannergress.backend.entities.Comment;

import java.util.UUID;

/** Service for comment-related tasks. */
public interface CommentService {
    /**
     * Creates a new comment, or updates an existing one.
     *
     * @param uuid       UUID of the comment.
     * @param banner     Banner for which the new comment should be created.
     * @param commentDto Comment data.
     * @return Created or updared comment.
     */
    Comment createOrUpdate(UUID uuid, Banner banner, CommentDto commentDto);

    /**
     * Deletes a comment.
     *
     * @param uuid UUID of the comment to delete.
     */
    void delete(UUID uuid);
}
