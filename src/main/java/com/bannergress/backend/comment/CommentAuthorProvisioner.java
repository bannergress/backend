package com.bannergress.backend.comment;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Creates an empty lock row independently; a concurrent first request may win the unique subject constraint. */
@Repository
public class CommentAuthorProvisioner {
    @PersistenceContext
    private EntityManager entities;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void ensure(String subject) {
        if (entities.createQuery("from CommentAuthor where subject = :subject", CommentAuthor.class)
            .setParameter("subject", subject).getResultList().isEmpty()) {
            var author = new CommentAuthor();
            author.subject = subject;
            entities.persist(author);
            entities.flush();
        }
    }
}
