package com.bannergress.backend.repositories;

import com.bannergress.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for users.
 */
public interface UserRepository extends JpaRepository<User, String> {

}
