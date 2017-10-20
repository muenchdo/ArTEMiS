package de.tum.in.www1.exerciseapp.repository;

import de.tum.in.www1.exerciseapp.domain.TeamManager;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the TeamManager entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TeamManagerRepository extends JpaRepository<TeamManager, Long> {

}
