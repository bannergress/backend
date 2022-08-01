package com.bannergress.backend.banner.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository for comments.
 */
public interface CommentRepository extends JpaRepository<Comment, UUID> {

}
