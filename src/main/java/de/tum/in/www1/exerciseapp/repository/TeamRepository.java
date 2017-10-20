package de.tum.in.www1.exerciseapp.repository;

import de.tum.in.www1.exerciseapp.domain.Team;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;

import java.util.List;


/**
 * Spring Data JPA repository for the Team entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByExerciseId(@Param("exerciseId") Long exerciseId);

    @Query("select t from Team t where t.exercise.course.id = :courseId")
    List<Team> findByCourseId(@Param("courseId") Long courseId);

    Team findOneByExerciseIdAndTeamName(Long exerciseId, String teamName);

    Team findOneByExerciseId(Long exerciseId);

    @Query("select team from Team team where team.max > ?#{team.current}")
    List<Team> findFreeByExerciseId(@Param("exerciseId") Long exerciseId);
}
