package de.tum.in.www1.exerciseapp.repository;

import de.tum.in.www1.exerciseapp.domain.ModelComparisonExercise;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the ModelComparisonExercise entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ModelComparisonExerciseRepository extends JpaRepository<ModelComparisonExercise,Long> {
    
}
