package de.tum.in.www1.exerciseapp.web.rest;

import com.codahale.metrics.annotation.Timed;
import de.tum.in.www1.exerciseapp.domain.ModelComparisonExercise;

import de.tum.in.www1.exerciseapp.domain.ProgrammingExercise;
import de.tum.in.www1.exerciseapp.repository.ModelComparisonExerciseRepository;
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

    public ModelComparisonExerciseResource(ModelComparisonExerciseRepository modelComparisonExerciseRepository) {
        this.modelComparisonExerciseRepository = modelComparisonExerciseRepository;
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
        ModelComparisonExercise result = modelComparisonExerciseRepository.save(modelComparisonExercise);
        return ResponseEntity.created(new URI("/api/model-comparison-exercises/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
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
        // TODO getModelComparisonExerciseForCourse()
        throw new RuntimeException("Not implemented yet");
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
