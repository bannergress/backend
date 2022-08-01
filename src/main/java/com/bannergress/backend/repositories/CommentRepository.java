package com.bannergress.backend.repositories;

import com.bannergress.backend.entities.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository for comments.
 */
public interface CommentRepository extends JpaRepository<Comment, UUID> {

}
