package com.bannergress.backend.repositories;

import com.bannergress.backend.entities.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repository for missions.
 */
public interface MissionRepository extends JpaRepository<Mission, String>, JpaSpecificationExecutor<Mission> {

}
