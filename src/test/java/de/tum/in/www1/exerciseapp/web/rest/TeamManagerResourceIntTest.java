package de.tum.in.www1.exerciseapp.web.rest;

import de.tum.in.www1.exerciseapp.ArTeMiSApp;

import de.tum.in.www1.exerciseapp.domain.TeamManager;
import de.tum.in.www1.exerciseapp.repository.TeamManagerRepository;
import de.tum.in.www1.exerciseapp.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the TeamManagerResource REST controller.
 *
 * @see TeamManagerResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ArTeMiSApp.class)
public class TeamManagerResourceIntTest {

    private static final String DEFAULT_TEAM_NAME = "AAAAAAAAAA";
    private static final String UPDATED_TEAM_NAME = "BBBBBBBBBB";

    private static final String DEFAULT_TEST = "AAAAAAAAAA";
    private static final String UPDATED_TEST = "BBBBBBBBBB";

    @Autowired
    private TeamManagerRepository teamManagerRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restTeamManagerMockMvc;

    private TeamManager teamManager;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final TeamManagerResource teamManagerResource = new TeamManagerResource(teamManagerRepository);
        this.restTeamManagerMockMvc = MockMvcBuilders.standaloneSetup(teamManagerResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static TeamManager createEntity(EntityManager em) {
        TeamManager teamManager = new TeamManager()
            .teamName(DEFAULT_TEAM_NAME)
            .test(DEFAULT_TEST);
        return teamManager;
    }

    @Before
    public void initTest() {
        teamManager = createEntity(em);
    }

    @Test
    @Transactional
    public void createTeamManager() throws Exception {
        int databaseSizeBeforeCreate = teamManagerRepository.findAll().size();

        // Create the TeamManager
        restTeamManagerMockMvc.perform(post("/api/team-managers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(teamManager)))
            .andExpect(status().isCreated());

        // Validate the TeamManager in the database
        List<TeamManager> teamManagerList = teamManagerRepository.findAll();
        assertThat(teamManagerList).hasSize(databaseSizeBeforeCreate + 1);
        TeamManager testTeamManager = teamManagerList.get(teamManagerList.size() - 1);
        assertThat(testTeamManager.getTeamName()).isEqualTo(DEFAULT_TEAM_NAME);
        assertThat(testTeamManager.getTest()).isEqualTo(DEFAULT_TEST);
    }

    @Test
    @Transactional
    public void createTeamManagerWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = teamManagerRepository.findAll().size();

        // Create the TeamManager with an existing ID
        teamManager.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restTeamManagerMockMvc.perform(post("/api/team-managers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(teamManager)))
            .andExpect(status().isBadRequest());

        // Validate the TeamManager in the database
        List<TeamManager> teamManagerList = teamManagerRepository.findAll();
        assertThat(teamManagerList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllTeamManagers() throws Exception {
        // Initialize the database
        teamManagerRepository.saveAndFlush(teamManager);

        // Get all the teamManagerList
        restTeamManagerMockMvc.perform(get("/api/team-managers?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(teamManager.getId().intValue())))
            .andExpect(jsonPath("$.[*].teamName").value(hasItem(DEFAULT_TEAM_NAME.toString())))
            .andExpect(jsonPath("$.[*].test").value(hasItem(DEFAULT_TEST.toString())));
    }

    @Test
    @Transactional
    public void getTeamManager() throws Exception {
        // Initialize the database
        teamManagerRepository.saveAndFlush(teamManager);

        // Get the teamManager
        restTeamManagerMockMvc.perform(get("/api/team-managers/{id}", teamManager.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(teamManager.getId().intValue()))
            .andExpect(jsonPath("$.teamName").value(DEFAULT_TEAM_NAME.toString()))
            .andExpect(jsonPath("$.test").value(DEFAULT_TEST.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingTeamManager() throws Exception {
        // Get the teamManager
        restTeamManagerMockMvc.perform(get("/api/team-managers/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateTeamManager() throws Exception {
        // Initialize the database
        teamManagerRepository.saveAndFlush(teamManager);
        int databaseSizeBeforeUpdate = teamManagerRepository.findAll().size();

        // Update the teamManager
        TeamManager updatedTeamManager = teamManagerRepository.findOne(teamManager.getId());
        updatedTeamManager
            .teamName(UPDATED_TEAM_NAME)
            .test(UPDATED_TEST);

        restTeamManagerMockMvc.perform(put("/api/team-managers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedTeamManager)))
            .andExpect(status().isOk());

        // Validate the TeamManager in the database
        List<TeamManager> teamManagerList = teamManagerRepository.findAll();
        assertThat(teamManagerList).hasSize(databaseSizeBeforeUpdate);
        TeamManager testTeamManager = teamManagerList.get(teamManagerList.size() - 1);
        assertThat(testTeamManager.getTeamName()).isEqualTo(UPDATED_TEAM_NAME);
        assertThat(testTeamManager.getTest()).isEqualTo(UPDATED_TEST);
    }

    @Test
    @Transactional
    public void updateNonExistingTeamManager() throws Exception {
        int databaseSizeBeforeUpdate = teamManagerRepository.findAll().size();

        // Create the TeamManager

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restTeamManagerMockMvc.perform(put("/api/team-managers")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(teamManager)))
            .andExpect(status().isCreated());

        // Validate the TeamManager in the database
        List<TeamManager> teamManagerList = teamManagerRepository.findAll();
        assertThat(teamManagerList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteTeamManager() throws Exception {
        // Initialize the database
        teamManagerRepository.saveAndFlush(teamManager);
        int databaseSizeBeforeDelete = teamManagerRepository.findAll().size();

        // Get the teamManager
        restTeamManagerMockMvc.perform(delete("/api/team-managers/{id}", teamManager.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<TeamManager> teamManagerList = teamManagerRepository.findAll();
        assertThat(teamManagerList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(TeamManager.class);
        TeamManager teamManager1 = new TeamManager();
        teamManager1.setId(1L);
        TeamManager teamManager2 = new TeamManager();
        teamManager2.setId(teamManager1.getId());
        assertThat(teamManager1).isEqualTo(teamManager2);
        teamManager2.setId(2L);
        assertThat(teamManager1).isNotEqualTo(teamManager2);
        teamManager1.setId(null);
        assertThat(teamManager1).isNotEqualTo(teamManager2);
    }
}
