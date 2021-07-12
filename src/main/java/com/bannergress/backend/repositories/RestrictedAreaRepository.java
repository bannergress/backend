package com.bannergress.backend.repositories;

import com.bannergress.backend.entities.RestrictedArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for restricted areas.
 */
@Repository
public interface RestrictedAreaRepository extends JpaRepository<RestrictedArea, UUID> {

}
