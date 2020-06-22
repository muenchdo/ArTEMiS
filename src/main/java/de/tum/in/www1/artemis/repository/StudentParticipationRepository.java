package de.tum.in.www1.artemis.repository;

import static org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.LOAD;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.tum.in.www1.artemis.domain.Exercise;
import de.tum.in.www1.artemis.domain.participation.StudentParticipation;

/**
 * Spring Data JPA repository for the Participation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface StudentParticipationRepository extends JpaRepository<StudentParticipation, Long> {

    List<StudentParticipation> findByExerciseId(@Param("exerciseId") Long exerciseId);

    boolean existsByExerciseId(@Param("exerciseId") Long exerciseId);

    @Query("select distinct participation from StudentParticipation participation left join fetch participation.results r where participation.exercise.course.id = :#{#courseId} and (r.rated is null or r.rated = true)")
    List<StudentParticipation> findByCourseIdWithEagerRatedResults(@Param("courseId") Long courseId);

    @Query("select distinct participation from StudentParticipation participation where participation.exercise.course.id = :#{#courseId} and participation.team.shortName = :#{#teamShortName}")
    List<StudentParticipation> findAllByCourseIdAndTeamShortName(@Param("courseId") Long courseId, @Param("teamShortName") String teamShortName);

    Optional<StudentParticipation> findByExerciseIdAndStudentLogin(Long exerciseId, String username);

    Optional<StudentParticipation> findByExerciseIdAndTeamId(Long exerciseId, Long teamId);

    List<StudentParticipation> findByTeamId(Long teamId);

    @EntityGraph(type = LOAD, attributePaths = "results")
    Optional<StudentParticipation> findWithEagerResultsByExerciseIdAndStudentLogin(Long exerciseId, String username);

    @EntityGraph(type = LOAD, attributePaths = "results")
    Optional<StudentParticipation> findWithEagerResultsByExerciseIdAndTeamId(Long exerciseId, Long teamId);

    @EntityGraph(type = LOAD, attributePaths = "submissions")
    Optional<StudentParticipation> findWithEagerSubmissionsByExerciseIdAndStudentLogin(Long exerciseId, String username);

    @EntityGraph(type = LOAD, attributePaths = "submissions")
    Optional<StudentParticipation> findWithEagerSubmissionsByExerciseIdAndTeamId(Long exerciseId, Long teamId);

    @Query("select distinct participation from StudentParticipation participation left join fetch participation.submissions s left join fetch s.result where participation.exercise.id = :#{#exerciseId}")
    List<StudentParticipation> findByExerciseIdWithEagerSubmissionsResult(@Param("exerciseId") Long exerciseId);

    @Query("select distinct participation from StudentParticipation participation left join fetch participation.submissions s left join fetch s.result r left join fetch r.assessor where participation.exercise.id = :#{#exerciseId}")
    List<StudentParticipation> findByExerciseIdWithEagerSubmissionsResultAssessor(@Param("exerciseId") Long exerciseId);

    /**
     * Get all participations for an exercise with each latest result (determined by id).
     * If there is no latest result (= no result at all), the participation will still be included in the returned ResultSet, but will have an empty Result array.
     *
     * @param exerciseId Exercise id.
     * @return participations for exercise.
     */
    @Query("select distinct participation from StudentParticipation participation left join fetch participation.results result where participation.exercise.id = :#{#exerciseId} and (result.id = (select max(id) from participation.results) or result is null)")
    List<StudentParticipation> findByExerciseIdWithLatestResult(@Param("exerciseId") Long exerciseId);

    @Query("select distinct participation from StudentParticipation participation left join fetch participation.results left join fetch participation.submissions where participation.exercise.id = :#{#exerciseId} and participation.student.id = :#{#studentId}")
    List<StudentParticipation> findByExerciseIdAndStudentIdWithEagerResultsAndSubmissions(@Param("exerciseId") Long exerciseId, @Param("studentId") Long studentId);

    @Query("select distinct participation from StudentParticipation participation left join fetch participation.results left join fetch participation.submissions where participation.exercise.id = :#{#exerciseId} and participation.team.id = :#{#teamId}")
    List<StudentParticipation> findByExerciseIdAndTeamIdWithEagerResultsAndSubmissions(@Param("exerciseId") Long exerciseId, @Param("teamId") Long teamId);

    @Query("select distinct participation from StudentParticipation participation left join fetch participation.results as par left join fetch par.feedbacks where participation.exercise.id = :#{#exerciseId} and participation.student.id = :#{#studentId} and (par.id = (select max(id) from participation.results) or par.id = null)")
    Optional<StudentParticipation> findByExerciseIdAndStudentIdWithLatestResult(@Param("exerciseId") Long exerciseId, @Param("studentId") Long studentId);

    @Query("select distinct participation from StudentParticipation participation left join fetch participation.results as par left join fetch par.feedbacks where participation.exercise.id = :#{#exerciseId} and participation.team.id = :#{#teamId} and (par.id = (select max(id) from participation.results) or par.id = null)")
    Optional<StudentParticipation> findByExerciseIdAndTeamIdWithLatestResult(@Param("exerciseId") Long exerciseId, @Param("teamId") Long teamId);

    /**
     * Find all participations of submissions that are submitted and do not already have a manual result. No manual result means that no user has started an assessment for the
     * corresponding submission yet.
     *
     * If a student can have multiple submissions per exercise type, the latest submission (by id) will be returned.
     *
     * @param exerciseId the exercise id the participations should belong to
     * @return a list of participations including their submitted submissions that do not have a manual result
     */
    @Query("select distinct participation from Participation participation left join fetch participation.submissions submission left join fetch submission.result result where participation.exercise.id = :#{#exerciseId} and not exists (select prs from participation.results prs where prs.assessmentType = 'MANUAL') and submission.submitted = true and submission.id = (select max(id) from participation.submissions)")
    List<StudentParticipation> findByExerciseIdWithLatestSubmissionWithoutManualResults(@Param("exerciseId") Long exerciseId);

    @Query("select distinct participation from StudentParticipation participation left join fetch participation.results where participation.id = :#{#participationId}")
    Optional<StudentParticipation> findByIdWithEagerResults(@Param("participationId") Long participationId);

    /**
     * Find the participation with the given id. Additionally, load all the submissions and results of the participation from the database. Returns an empty Optional if the
     * participation could not be found.
     *
     * @param participationId the id of the participation
     * @return the participation with eager submissions and results or an empty Optional
     */
    @EntityGraph(type = LOAD, attributePaths = { "submissions", "submissions.result", "results", "team.students" })
    Optional<StudentParticipation> findWithEagerSubmissionsAndResultsById(Long participationId);

    /**
     * Find the participation with the given id. Additionally, load all the submissions and results of the participation from the database.
     * Further, load the exercise and its course. Returns an empty Optional if the participation could not be found.
     *
     * @param participationId the id of the participation
     * @return the participation with eager submissions, results, exercise and course or an empty Optional
     */
    @EntityGraph(type = LOAD, attributePaths = { "submissions", "submissions.result", "submissions.result.feedbacks", "results", "exercise", "exercise.course", "team.students" })
    Optional<StudentParticipation> findWithEagerSubmissionsAndResultsAndExerciseAndCourseById(Long participationId);

    @EntityGraph(type = LOAD, attributePaths = { "submissions", "results", "results.assessor" })
    Optional<StudentParticipation> findWithEagerSubmissionsAndResultsAssessorsById(Long participationId);

    @EntityGraph(type = LOAD, attributePaths = { "submissions", "submissions.result", "submissions.result.assessor" })
    List<StudentParticipation> findAllWithEagerSubmissionsAndEagerResultsAndEagerAssessorByExerciseId(long exerciseId);

    @Query("SELECT DISTINCT participation FROM StudentParticipation participation LEFT JOIN FETCH participation.exercise e LEFT JOIN FETCH e.course WHERE participation.id = :#{#participationId}")
    StudentParticipation findOneByIdWithEagerExerciseAndEagerCourse(@Param("participationId") Long participationId);

    @Query("SELECT DISTINCT participation FROM StudentParticipation participation LEFT JOIN FETCH participation.results LEFT JOIN FETCH participation.exercise e LEFT JOIN FETCH e.course WHERE participation.id = :#{#participationId}")
    StudentParticipation findOneByIdWithEagerResultsAndExerciseAndEagerCourse(@Param("participationId") Long participationId);

    @Query("select distinct p from StudentParticipation p left join fetch p.submissions s left join fetch s.result r where p.student.id = :#{#studentId} and p.exercise in :#{#exercises}")
    List<StudentParticipation> findByStudentIdAndIndividualExercisesWithEagerSubmissionsResult(@Param("studentId") Long studentId, @Param("exercises") List<Exercise> exercises);

    @Query("select distinct p from StudentParticipation p left join fetch p.submissions s left join fetch s.result r left join fetch p.team t left join fetch t.students teamStudent where teamStudent.id = :#{#studentId} and p.exercise in :#{#exercises}")
    List<StudentParticipation> findByStudentIdAndTeamExercisesWithEagerSubmissionsResult(@Param("studentId") Long studentId, @Param("exercises") List<Exercise> exercises);

    @Query("select distinct p from StudentParticipation p left join fetch p.submissions s left join fetch s.result r left join fetch p.team t where p.exercise.course.id = :#{#courseId} and t.shortName = :#{#teamShortName}")
    List<StudentParticipation> findAllByCourseIdAndTeamShortNameWithEagerSubmissionsResult(@Param("courseId") Long courseId, @Param("teamShortName") String teamShortName);

    @EntityGraph(type = LOAD, attributePaths = { "submissions", "submissions.result", "submissions.result.assessor" })
    @Query("select distinct p from StudentParticipation p left join fetch p.submissions s where p.exercise.id = :#{#exerciseId} and (s.result.assessor.id = :#{#assessorId} and s.id = (select max(id) from p.submissions) or s.id = null)")
    List<StudentParticipation> findWithLatestSubmissionByExerciseAndAssessor(@Param("exerciseId") Long exerciseId, @Param("assessorId") Long assessorId);

    /**
     * Count the number of submissions for each participation in a given exercise.
     *
     * @param exerciseId the id of the exercise for which to consider participations
     * @return Tuples of participation ids and number of submissions per participation
     */
    @Query("select participation.id, count(submissions) from StudentParticipation participation left join participation.submissions submissions where participation.exercise.id = :#{#exerciseId} group by participation.id")
    List<long[]> countSubmissionsPerParticipationByExerciseId(@Param("exerciseId") long exerciseId);

    /**
     * Count the number of submissions for each participation for a given team in a course
     *
     * @param courseId the id of the course for which to consider participations
     * @param teamShortName the short name of the team for which to consider participations
     * @return Tuples of participation ids and number of submissions per participation
     */
    @Query("select participation.id, count(submissions) from StudentParticipation participation left join participation.submissions submissions where participation.team.shortName = :#{#teamShortName} and participation.exercise.course.id = :#{#courseId} group by participation.id")
    List<long[]> countSubmissionsPerParticipationByCourseIdAndTeamShortName(@Param("courseId") long courseId, @Param("teamShortName") String teamShortName);
}
