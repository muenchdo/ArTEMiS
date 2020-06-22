package de.tum.in.www1.artemis.web.rest;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.tum.in.www1.artemis.domain.LtiUserId;
import de.tum.in.www1.artemis.repository.LtiUserIdRepository;
import de.tum.in.www1.artemis.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing LtiUserId.
 */
@RestController
@RequestMapping("/api")
public class LtiUserIdResource {

    private final Logger log = LoggerFactory.getLogger(LtiUserIdResource.class);

    private static final String ENTITY_NAME = "ltiUserId";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private LtiUserIdRepository ltiUserIdRepository;

    public LtiUserIdResource(LtiUserIdRepository ltiUserIdRepository) {
        this.ltiUserIdRepository = ltiUserIdRepository;
    }

    /**
     * POST /lti-user-ids : Create a new ltiUserId.
     *
     * @param ltiUserId the ltiUserId to create
     * @return the ResponseEntity with status 201 (Created) and with body the new ltiUserId, or with status 400 (Bad Request) if the ltiUserId has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/lti-user-ids")
    public ResponseEntity<LtiUserId> createLtiUserId(@RequestBody LtiUserId ltiUserId) throws URISyntaxException {
        log.debug("REST request to save LtiUserId : {}", ltiUserId);
        if (ltiUserId.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(applicationName, true, ENTITY_NAME, "idexists", "A new ltiUserId cannot already have an ID"))
                    .body(null);
        }
        LtiUserId result = ltiUserIdRepository.save(ltiUserId);
        return ResponseEntity.created(new URI("/api/lti-user-ids/" + result.getId()))
                .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString())).body(result);
    }

    /**
     * PUT /lti-user-ids : Updates an existing ltiUserId.
     *
     * @param ltiUserId the ltiUserId to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated ltiUserId, or with status 400 (Bad Request) if the ltiUserId is not valid, or with status 500
     *         (Internal Server Error) if the ltiUserId couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/lti-user-ids")
    public ResponseEntity<LtiUserId> updateLtiUserId(@RequestBody LtiUserId ltiUserId) throws URISyntaxException {
        log.debug("REST request to update LtiUserId : {}", ltiUserId);
        if (ltiUserId.getId() == null) {
            return createLtiUserId(ltiUserId);
        }
        LtiUserId result = ltiUserIdRepository.save(ltiUserId);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, ltiUserId.getId().toString())).body(result);
    }

    /**
     * GET /lti-user-ids/:id : get the "id" ltiUserId.
     *
     * @param id the id of the ltiUserId to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the ltiUserId, or with status 404 (Not Found)
     */
    @GetMapping("/lti-user-ids/{id}")
    public ResponseEntity<LtiUserId> getLtiUserId(@PathVariable Long id) {
        log.debug("REST request to get LtiUserId : {}", id);
        Optional<LtiUserId> ltiUserId = ltiUserIdRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(ltiUserId);
    }

    /**
     * DELETE /lti-user-ids/:id : delete the "id" ltiUserId.
     *
     * @param id the id of the ltiUserId to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/lti-user-ids/{id}")
    public ResponseEntity<Void> deleteLtiUserId(@PathVariable Long id) {
        log.debug("REST request to delete LtiUserId : {}", id);
        ltiUserIdRepository.deleteById(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build();
    }
}
