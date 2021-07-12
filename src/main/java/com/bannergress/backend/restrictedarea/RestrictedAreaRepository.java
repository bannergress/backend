package com.bannergress.backend.restrictedarea;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for restricted areas.
 */
@Repository
public interface RestrictedAreaRepository
    extends JpaRepository<RestrictedArea, UUID>, JpaSpecificationExecutor<RestrictedArea> {

}
