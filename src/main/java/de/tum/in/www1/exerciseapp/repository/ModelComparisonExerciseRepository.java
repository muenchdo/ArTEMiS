package de.tum.in.www1.exerciseapp.repository;

import de.tum.in.www1.exerciseapp.domain.ModelComparisonExercise;
import de.tum.in.www1.exerciseapp.domain.ProgrammingExercise;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;


/**
 * Spring Data JPA repository for the ModelComparisonExercise entity.
 */
@SuppressWarnings("unused")
@Repository
public interface ModelComparisonExerciseRepository extends JpaRepository<ModelComparisonExercise, Long> {

    @Query("SELECT e FROM ModelComparisonExercise e WHERE e.course.id = :#{#courseId}")
    List<ModelComparisonExercise> findByCourseId(@Param("courseId") Long courseId);
}
