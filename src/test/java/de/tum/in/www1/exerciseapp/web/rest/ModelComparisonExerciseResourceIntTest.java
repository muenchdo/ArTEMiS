package de.tum.in.www1.exerciseapp.web.rest;

import de.tum.in.www1.exerciseapp.ArTeMiSApp;

import de.tum.in.www1.exerciseapp.domain.ModelComparisonExercise;
import de.tum.in.www1.exerciseapp.repository.ModelComparisonExerciseRepository;
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
 * Test class for the ModelComparisonExerciseResource REST controller.
 *
 * @see ModelComparisonExerciseResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ArTeMiSApp.class)
public class ModelComparisonExerciseResourceIntTest {

    private static final String DEFAULT_BASE_REPOSITORY_URL = "AAAAAAAAAA";
    private static final String UPDATED_BASE_REPOSITORY_URL = "BBBBBBBBBB";

    private static final String DEFAULT_BASE_BUILD_PLAN_ID = "AAAAAAAAAA";
    private static final String UPDATED_BASE_BUILD_PLAN_ID = "BBBBBBBBBB";

    @Autowired
    private ModelComparisonExerciseRepository modelComparisonExerciseRepository;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restModelComparisonExerciseMockMvc;

    private ModelComparisonExercise modelComparisonExercise;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ModelComparisonExerciseResource modelComparisonExerciseResource = new ModelComparisonExerciseResource(modelComparisonExerciseRepository);
        this.restModelComparisonExerciseMockMvc = MockMvcBuilders.standaloneSetup(modelComparisonExerciseResource)
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
    public static ModelComparisonExercise createEntity(EntityManager em) {
        ModelComparisonExercise modelComparisonExercise = new ModelComparisonExercise()
            .baseRepositoryUrl(DEFAULT_BASE_REPOSITORY_URL)
            .baseBuildPlanId(DEFAULT_BASE_BUILD_PLAN_ID);
        return modelComparisonExercise;
    }

    @Before
    public void initTest() {
        modelComparisonExercise = createEntity(em);
    }

    @Test
    @Transactional
    public void createModelComparisonExercise() throws Exception {
        int databaseSizeBeforeCreate = modelComparisonExerciseRepository.findAll().size();

        // Create the ModelComparisonExercise
        restModelComparisonExerciseMockMvc.perform(post("/api/model-comparison-exercises")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(modelComparisonExercise)))
            .andExpect(status().isCreated());

        // Validate the ModelComparisonExercise in the database
        List<ModelComparisonExercise> modelComparisonExerciseList = modelComparisonExerciseRepository.findAll();
        assertThat(modelComparisonExerciseList).hasSize(databaseSizeBeforeCreate + 1);
        ModelComparisonExercise testModelComparisonExercise = modelComparisonExerciseList.get(modelComparisonExerciseList.size() - 1);
        assertThat(testModelComparisonExercise.getBaseRepositoryUrl()).isEqualTo(DEFAULT_BASE_REPOSITORY_URL);
        assertThat(testModelComparisonExercise.getBaseBuildPlanId()).isEqualTo(DEFAULT_BASE_BUILD_PLAN_ID);
    }

    @Test
    @Transactional
    public void createModelComparisonExerciseWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = modelComparisonExerciseRepository.findAll().size();

        // Create the ModelComparisonExercise with an existing ID
        modelComparisonExercise.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restModelComparisonExerciseMockMvc.perform(post("/api/model-comparison-exercises")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(modelComparisonExercise)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<ModelComparisonExercise> modelComparisonExerciseList = modelComparisonExerciseRepository.findAll();
        assertThat(modelComparisonExerciseList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void getAllModelComparisonExercises() throws Exception {
        // Initialize the database
        modelComparisonExerciseRepository.saveAndFlush(modelComparisonExercise);

        // Get all the modelComparisonExerciseList
        restModelComparisonExerciseMockMvc.perform(get("/api/model-comparison-exercises?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(modelComparisonExercise.getId().intValue())))
            .andExpect(jsonPath("$.[*].baseRepositoryUrl").value(hasItem(DEFAULT_BASE_REPOSITORY_URL.toString())))
            .andExpect(jsonPath("$.[*].baseBuildPlanId").value(hasItem(DEFAULT_BASE_BUILD_PLAN_ID.toString())));
    }

    @Test
    @Transactional
    public void getModelComparisonExercise() throws Exception {
        // Initialize the database
        modelComparisonExerciseRepository.saveAndFlush(modelComparisonExercise);

        // Get the modelComparisonExercise
        restModelComparisonExerciseMockMvc.perform(get("/api/model-comparison-exercises/{id}", modelComparisonExercise.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(modelComparisonExercise.getId().intValue()))
            .andExpect(jsonPath("$.baseRepositoryUrl").value(DEFAULT_BASE_REPOSITORY_URL.toString()))
            .andExpect(jsonPath("$.baseBuildPlanId").value(DEFAULT_BASE_BUILD_PLAN_ID.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingModelComparisonExercise() throws Exception {
        // Get the modelComparisonExercise
        restModelComparisonExerciseMockMvc.perform(get("/api/model-comparison-exercises/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateModelComparisonExercise() throws Exception {
        // Initialize the database
        modelComparisonExerciseRepository.saveAndFlush(modelComparisonExercise);
        int databaseSizeBeforeUpdate = modelComparisonExerciseRepository.findAll().size();

        // Update the modelComparisonExercise
        ModelComparisonExercise updatedModelComparisonExercise = modelComparisonExerciseRepository.findOne(modelComparisonExercise.getId());
        updatedModelComparisonExercise
            .baseRepositoryUrl(UPDATED_BASE_REPOSITORY_URL)
            .baseBuildPlanId(UPDATED_BASE_BUILD_PLAN_ID);

        restModelComparisonExerciseMockMvc.perform(put("/api/model-comparison-exercises")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedModelComparisonExercise)))
            .andExpect(status().isOk());

        // Validate the ModelComparisonExercise in the database
        List<ModelComparisonExercise> modelComparisonExerciseList = modelComparisonExerciseRepository.findAll();
        assertThat(modelComparisonExerciseList).hasSize(databaseSizeBeforeUpdate);
        ModelComparisonExercise testModelComparisonExercise = modelComparisonExerciseList.get(modelComparisonExerciseList.size() - 1);
        assertThat(testModelComparisonExercise.getBaseRepositoryUrl()).isEqualTo(UPDATED_BASE_REPOSITORY_URL);
        assertThat(testModelComparisonExercise.getBaseBuildPlanId()).isEqualTo(UPDATED_BASE_BUILD_PLAN_ID);
    }

    @Test
    @Transactional
    public void updateNonExistingModelComparisonExercise() throws Exception {
        int databaseSizeBeforeUpdate = modelComparisonExerciseRepository.findAll().size();

        // Create the ModelComparisonExercise

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restModelComparisonExerciseMockMvc.perform(put("/api/model-comparison-exercises")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(modelComparisonExercise)))
            .andExpect(status().isCreated());

        // Validate the ModelComparisonExercise in the database
        List<ModelComparisonExercise> modelComparisonExerciseList = modelComparisonExerciseRepository.findAll();
        assertThat(modelComparisonExerciseList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteModelComparisonExercise() throws Exception {
        // Initialize the database
        modelComparisonExerciseRepository.saveAndFlush(modelComparisonExercise);
        int databaseSizeBeforeDelete = modelComparisonExerciseRepository.findAll().size();

        // Get the modelComparisonExercise
        restModelComparisonExerciseMockMvc.perform(delete("/api/model-comparison-exercises/{id}", modelComparisonExercise.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<ModelComparisonExercise> modelComparisonExerciseList = modelComparisonExerciseRepository.findAll();
        assertThat(modelComparisonExerciseList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(ModelComparisonExercise.class);
        ModelComparisonExercise modelComparisonExercise1 = new ModelComparisonExercise();
        modelComparisonExercise1.setId(1L);
        ModelComparisonExercise modelComparisonExercise2 = new ModelComparisonExercise();
        modelComparisonExercise2.setId(modelComparisonExercise1.getId());
        assertThat(modelComparisonExercise1).isEqualTo(modelComparisonExercise2);
        modelComparisonExercise2.setId(2L);
        assertThat(modelComparisonExercise1).isNotEqualTo(modelComparisonExercise2);
        modelComparisonExercise1.setId(null);
        assertThat(modelComparisonExercise1).isNotEqualTo(modelComparisonExercise2);
    }
}
