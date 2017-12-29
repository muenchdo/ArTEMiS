package de.tum.in.www1.exerciseapp.web.rest;

import com.codahale.metrics.annotation.Timed;
import de.tum.in.www1.exerciseapp.domain.*;

import de.tum.in.www1.exerciseapp.repository.ModelComparisonExerciseRepository;
import de.tum.in.www1.exerciseapp.service.ContinuousIntegrationService;
import de.tum.in.www1.exerciseapp.service.CourseService;
import de.tum.in.www1.exerciseapp.service.UserService;
import de.tum.in.www1.exerciseapp.service.VersionControlService;
import de.tum.in.www1.exerciseapp.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing ModelComparisonExercise.
 */
@RestController
@RequestMapping("/api")
public class ModelComparisonExerciseResource {

    private final Logger log = LoggerFactory.getLogger(ModelComparisonExerciseResource.class);

    private static final String ENTITY_NAME = "modelComparisonExercise";

    private final ModelComparisonExerciseRepository modelComparisonExerciseRepository;
    private final UserService userService;
    private final CourseService courseService;
    private final Optional<ContinuousIntegrationService> continuousIntegrationService;
    private final Optional<VersionControlService> versionControlService;



    public ModelComparisonExerciseResource(ModelComparisonExerciseRepository modelComparisonExerciseRepository, UserService userService, CourseService courseService, Optional<ContinuousIntegrationService> continuousIntegrationService, Optional<VersionControlService> versionControlService) {
        this.modelComparisonExerciseRepository = modelComparisonExerciseRepository;
        this.userService = userService;
        this.courseService = courseService;
        this.continuousIntegrationService = continuousIntegrationService;
        this.versionControlService = versionControlService;
    }

    /**
     * POST  /model-comparison-exercises : Create a new modelComparisonExercise.
     *
     * @param modelComparisonExercise the modelComparisonExercise to create
     * @return the ResponseEntity with status 201 (Created) and with body the new modelComparisonExercise, or with status 400 (Bad Request) if the modelComparisonExercise has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/model-comparison-exercises")
    @PreAuthorize("hasAnyRole('TA', 'ADMIN')")
    @Timed
    public ResponseEntity<ModelComparisonExercise> createModelComparisonExercise(@RequestBody ModelComparisonExercise modelComparisonExercise) throws URISyntaxException {
        log.debug("REST request to save ModelComparisonExercise : {}", modelComparisonExercise);
        if (modelComparisonExercise.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new modelComparisonExercise cannot already have an ID")).body(null);
        }

        ResponseEntity<ModelComparisonExercise> errorResponse = checkProgrammingExerciseForError(modelComparisonExercise);
        if(errorResponse != null) {
            return errorResponse;
        }

        ModelComparisonExercise result = modelComparisonExerciseRepository.save(modelComparisonExercise);
        return ResponseEntity.created(new URI("/api/model-comparison-exercises/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     *
     * @param exercise the exercise object we want to check for errors
     * @return the error message as response or null if everything is fine
     */
    private ResponseEntity<ModelComparisonExercise> checkProgrammingExerciseForError(ModelComparisonExercise exercise) {
        if(continuousIntegrationService.isPresent() && !continuousIntegrationService.get().buildPlanIdIsValid(exercise.getBaseBuildPlanId())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("exercise", "invalid.build.plan.id", "The Base Build Plan ID seems to be invalid.")).body(null);
        }
        if(versionControlService.isPresent() && !versionControlService.get().repositoryUrlIsValid(exercise.getBaseRepositoryUrlAsUrl())) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert("exercise", "invalid.repository.url", "The Repository URL seems to be invalid.")).body(null);
        }
        return null;
    }

    /**
     * PUT  /model-comparison-exercises : Updates an existing modelComparisonExercise.
     *
     * @param modelComparisonExercise the modelComparisonExercise to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated modelComparisonExercise,
     * or with status 400 (Bad Request) if the modelComparisonExercise is not valid,
     * or with status 500 (Internal Server Error) if the modelComparisonExercise couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/model-comparison-exercises")
    @PreAuthorize("hasAnyRole('TA', 'ADMIN')")
    @Timed
    public ResponseEntity<ModelComparisonExercise> updateModelComparisonExercise(@RequestBody ModelComparisonExercise modelComparisonExercise) throws URISyntaxException {
        log.debug("REST request to update ModelComparisonExercise : {}", modelComparisonExercise);
        if (modelComparisonExercise.getId() == null) {
            return createModelComparisonExercise(modelComparisonExercise);
        }

        ResponseEntity<ModelComparisonExercise> errorResponse = checkProgrammingExerciseForError(modelComparisonExercise);
        if(errorResponse != null) {
            return errorResponse;
        }

        ModelComparisonExercise result = modelComparisonExerciseRepository.save(modelComparisonExercise);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, modelComparisonExercise.getId().toString()))
            .body(result);
    }

    /**
     * GET  /model-comparison-exercises : get all the modelComparisonExercises.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of modelComparisonExercises in body
     */
    @GetMapping("/model-comparison-exercises")
    @PreAuthorize("hasAnyRole('TA', 'ADMIN')")
    @Timed
    public List<ModelComparisonExercise> getAllModelComparisonExercises() {
        log.debug("REST request to get all ModelComparisonExercises");
        return modelComparisonExerciseRepository.findAll();
    }

    /**
     * GET /courses/:courseId/model-comparison-exercise
     *
     * @param courseId the id of the course
     * @return the ResponseEntity with status 200 (OK) and the list of modelComparisonExercises for the given course in  the body
     */
    @GetMapping(value = "/courses/{courseId}/model-comparison-exercises")
    @PreAuthorize("hasAnyRole('TA', 'ADMIN')")
    @Timed
    @Transactional(readOnly = true)
    public List<ModelComparisonExercise> getProgrammingExercisesForCourse(@PathVariable Long courseId) {
        log.debug("REST request to get all ModelComparisonExercises for the course with id : {}", courseId);

        //this call is only used in the admin interface and there, tutors should not see exercise of courses in which they are only students
        User user = userService.getUserWithGroupsAndAuthorities();
        Authority adminAuthority = new Authority();
        adminAuthority.setName("ROLE_ADMIN");
        Authority taAuthority = new Authority();
        taAuthority.setName("ROLE_TA");

        // get the course
        Course course = courseService.findOne(courseId);

        // determine user's access level for this course
        if (user.getAuthorities().contains(adminAuthority)) {
            // user is admin
            return modelComparisonExerciseRepository.findByCourseId(courseId);
        } else if (user.getAuthorities().contains(taAuthority) && user.getGroups().contains(course.getTeachingAssistantGroupName())) {
            // user is TA for this course
            return modelComparisonExerciseRepository.findByCourseId(courseId);
        }
        //in this case the user does not have access, return an empty list
        return Collections.emptyList();
    }

    /**
     * GET  /model-comparison-exercises/:id : get the "id" modelComparisonExercise.
     *
     * @param id the id of the modelComparisonExercise to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the modelComparisonExercise, or with status 404 (Not Found)
     */
    @GetMapping("/model-comparison-exercises/{id}")
    @PreAuthorize("hasAnyRole('TA', 'ADMIN')")
    @Timed
    public ResponseEntity<ModelComparisonExercise> getModelComparisonExercise(@PathVariable Long id) {
        log.debug("REST request to get ModelComparisonExercise : {}", id);
        ModelComparisonExercise modelComparisonExercise = modelComparisonExerciseRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(modelComparisonExercise));
    }


    /**
     * DELETE  /model-comparison-exercises/:id : delete the "id" modelComparisonExercise.
     *
     * @param id the id of the modelComparisonExercise to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/model-comparison-exercises/{id}")
    @PreAuthorize("hasAnyRole('TA', 'ADMIN')")
    @Timed
    public ResponseEntity<Void> deleteModelComparisonExercise(@PathVariable Long id) {
        log.debug("REST request to delete ModelComparisonExercise : {}", id);
        modelComparisonExerciseRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
