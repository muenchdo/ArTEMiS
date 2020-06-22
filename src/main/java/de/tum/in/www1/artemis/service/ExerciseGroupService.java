package de.tum.in.www1.artemis.service;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import de.tum.in.www1.artemis.domain.Course;
import de.tum.in.www1.artemis.domain.exam.ExerciseGroup;
import de.tum.in.www1.artemis.repository.ExerciseGroupRepository;
import de.tum.in.www1.artemis.web.rest.errors.EntityNotFoundException;

/**
 * Service Implementation for managing ExerciseGroup.
 */
@Service
public class ExerciseGroupService {

    private final Logger log = LoggerFactory.getLogger(ExerciseGroupService.class);

    private final ExerciseGroupRepository exerciseGroupRepository;

    public ExerciseGroupService(ExerciseGroupRepository exerciseGroupRepository) {
        this.exerciseGroupRepository = exerciseGroupRepository;
    }

    /**
     * Save an exerciseGroup
     *
     * @param exerciseGroup the entity to save
     * @return the persisted entity
     */
    public ExerciseGroup save(ExerciseGroup exerciseGroup) {
        log.debug("Request to save exerciseGroup : {}", exerciseGroup);
        return exerciseGroupRepository.save(exerciseGroup);
    }

    /**
     * Get one exercise group by id.
     *
     * @param exerciseGroupId the id of the exercise group
     * @return the entity
     */
    @NotNull
    public ExerciseGroup findOne(Long exerciseGroupId) {
        log.debug("Request to get exercise group : {}", exerciseGroupId);
        return exerciseGroupRepository.findById(exerciseGroupId)
                .orElseThrow(() -> new EntityNotFoundException("Exercise group with id \"" + exerciseGroupId + "\" does not exist"));
    }

    /**
     * Get one exerciseGroup by id with the corresponding exam.
     *
     * @param exerciseGroupId the id of the entity
     * @return the entity
     */
    @NotNull
    public ExerciseGroup findOneWithExam(Long exerciseGroupId) {
        log.debug("Request to get exerciseGroup with exam : {}", exerciseGroupId);
        return exerciseGroupRepository.findWithEagerExamById(exerciseGroupId)
                .orElseThrow(() -> new EntityNotFoundException("ExerciseGroup with id: \"" + exerciseGroupId + "\" does not exist"));
    }

    /**
     * Get one exerciseGroup by id with all exercises.
     *
     * @param exerciseGroupId the id of the entity
     * @return the exercise group with all exercise
     */
    @NotNull
    public ExerciseGroup findOneWithExercises(Long exerciseGroupId) {
        log.debug("Request to get exerciseGroup with exam : {}", exerciseGroupId);
        return exerciseGroupRepository.findWithEagerExercisesById(exerciseGroupId)
                .orElseThrow(() -> new EntityNotFoundException("ExerciseGroup with id: \"" + exerciseGroupId + "\" does not exist"));
    }

    /**
     * Get all exercise groups for the given exam with all exercises.
     *
     * @param examId the id of the exam
     * @return the list of all exercise groups
     */
    public List<ExerciseGroup> findAllWithExamAndExercises(Long examId) {
        log.debug("REST request to get all exercise groups for Exam : {}", examId);
        return exerciseGroupRepository.findWithEagerExamAndExercisesByExamId(examId);
    }

    /**
     * Delete the exercise group by id.
     *
     * @param exerciseGroupId the id of the entity
     */
    public void delete(Long exerciseGroupId) {
        log.debug("Request to delete exercise group : {}", exerciseGroupId);
        exerciseGroupRepository.deleteById(exerciseGroupId);
    }

    /**
     * Retrieve the course through ExerciseGroup -> Exam -> Course
     *
     * @param exerciseGroupId the id of the exerciseGroup for which the course is retrieved
     * @return the Course of the Exercise
     */
    public Course retrieveCourseOverExerciseGroup(Long exerciseGroupId) {
        ExerciseGroup exerciseGroup = findOneWithExam(exerciseGroupId);
        return exerciseGroup.getExam().getCourse();
    }
}
