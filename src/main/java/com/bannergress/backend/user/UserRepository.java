package com.bannergress.backend.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for users.
 */
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findOneByVerificationAgentIgnoreCaseAndVerificationToken(String verificationAgent,
                                                                            UUID verificationToken);
}
