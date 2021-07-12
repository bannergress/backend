package com.bannergress.backend.repositories;

import com.bannergress.backend.entities.POI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repository for POI.
 */
public interface POIRepository extends JpaRepository<POI, String>, JpaSpecificationExecutor<POI> {

}
