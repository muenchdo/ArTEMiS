package de.tum.in.www1.exerciseapp.web.rest;

import com.codahale.metrics.annotation.Timed;
import de.tum.in.www1.exerciseapp.domain.TeamManager;

import de.tum.in.www1.exerciseapp.repository.TeamManagerRepository;
import de.tum.in.www1.exerciseapp.web.rest.util.HeaderUtil;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing TeamManager.
 */
@RestController
@RequestMapping("/api")
public class TeamManagerResource {

    private final Logger log = LoggerFactory.getLogger(TeamManagerResource.class);

    private static final String ENTITY_NAME = "teamManager";

    private final TeamManagerRepository teamManagerRepository;

    public TeamManagerResource(TeamManagerRepository teamManagerRepository) {
        this.teamManagerRepository = teamManagerRepository;
    }

    /**
     * POST  /team-managers : Create a new teamManager.
     *
     * @param teamManager the teamManager to create
     * @return the ResponseEntity with status 201 (Created) and with body the new teamManager, or with status 400 (Bad Request) if the teamManager has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/team-managers")
    @Timed
    public ResponseEntity<TeamManager> createTeamManager(@RequestBody TeamManager teamManager) throws URISyntaxException {
        log.debug("REST request to save TeamManager : {}", teamManager);
        if (teamManager.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new teamManager cannot already have an ID")).body(null);
        }
        TeamManager result = teamManagerRepository.save(teamManager);
        return ResponseEntity.created(new URI("/api/team-managers/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /team-managers : Updates an existing teamManager.
     *
     * @param teamManager the teamManager to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated teamManager,
     * or with status 400 (Bad Request) if the teamManager is not valid,
     * or with status 500 (Internal Server Error) if the teamManager couldn't be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/team-managers")
    @Timed
    public ResponseEntity<TeamManager> updateTeamManager(@RequestBody TeamManager teamManager) throws URISyntaxException {
        log.debug("REST request to update TeamManager : {}", teamManager);
        if (teamManager.getId() == null) {
            return createTeamManager(teamManager);
        }
        TeamManager result = teamManagerRepository.save(teamManager);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, teamManager.getId().toString()))
            .body(result);
    }

    /**
     * GET  /team-managers : get all the teamManagers.
     *
     * @return the ResponseEntity with status 200 (OK) and the list of teamManagers in body
     */
    @GetMapping("/team-managers")
    @Timed
    public List<TeamManager> getAllTeamManagers() {
        log.debug("REST request to get all TeamManagers");
        return teamManagerRepository.findAll();
        }

    /**
     * GET  /team-managers/:id : get the "id" teamManager.
     *
     * @param id the id of the teamManager to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the teamManager, or with status 404 (Not Found)
     */
    @GetMapping("/team-managers/{id}")
    @Timed
    public ResponseEntity<TeamManager> getTeamManager(@PathVariable Long id) {
        log.debug("REST request to get TeamManager : {}", id);
        TeamManager teamManager = teamManagerRepository.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(teamManager));
    }

    /**
     * DELETE  /team-managers/:id : delete the "id" teamManager.
     *
     * @param id the id of the teamManager to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/team-managers/{id}")
    @Timed
    public ResponseEntity<Void> deleteTeamManager(@PathVariable Long id) {
        log.debug("REST request to delete TeamManager : {}", id);
        teamManagerRepository.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }
}
