package de.tum.in.www1.artemis;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;

import de.tum.in.www1.artemis.domain.*;
import de.tum.in.www1.artemis.domain.enumeration.TutorParticipationStatus;
import de.tum.in.www1.artemis.domain.modeling.ModelingExercise;
import de.tum.in.www1.artemis.domain.participation.TutorParticipation;
import de.tum.in.www1.artemis.repository.*;
import de.tum.in.www1.artemis.service.ExampleSubmissionService;
import de.tum.in.www1.artemis.util.DatabaseUtilService;
import de.tum.in.www1.artemis.util.RequestUtilService;

public class TutorParticipationIntegrationTest extends AbstractSpringIntegrationBambooBitbucketJiraTest {

    @Autowired
    ExerciseRepository exerciseRepo;

    @Autowired
    RequestUtilService request;

    @Autowired
    DatabaseUtilService database;

    @Autowired
    ExampleSubmissionService exampleSubmissionService;

    private ModelingExercise modelingExercise;

    private ExampleSubmission exampleSubmission;

    @BeforeEach
    public void initTestCase() throws Exception {
        database.addUsers(0, 1, 0);
        Course course = database.addCourseWithModelingAndTextExercise();
        for (Exercise exercise : course.getExercises()) {
            if (exercise instanceof ModelingExercise) {
                modelingExercise = (ModelingExercise) exercise;
            }
        }
        modelingExercise.setTitle("UML Class Diagram");
        exerciseRepo.save(modelingExercise);

        String validModel = database.loadFileFromResources("test-data/model-submission/model.54727.json");
        exampleSubmission = database.generateExampleSubmission(validModel, modelingExercise, false);
        exampleSubmissionService.save(exampleSubmission);
    }

    @AfterEach
    public void tearDown() {
        database.resetDatabase();
    }

    @Test
    @WithMockUser(username = "tutor1", roles = "TA")
    public void testTutorParticipateInModelingExerciseWithExampleSubmission() throws Exception {
        request.postWithResponseBody("/api/exercises/" + modelingExercise.getId() + "/tutorParticipations", null, TutorParticipation.class, HttpStatus.CREATED);
        TutorParticipation tutorParticipation = request.postWithResponseBody("/api/exercises/" + modelingExercise.getId() + "/exampleSubmission", exampleSubmission,
                TutorParticipation.class, HttpStatus.OK);
        assertThat(tutorParticipation.getTrainedExampleSubmissions()).as("Tutor participation has example submission").hasSize(1);
        assertThat(tutorParticipation.getTutor().getLogin()).as("Tutor participation belongs to correct tutor").isEqualTo("tutor1");
        assertThat(tutorParticipation.getAssessedExercise()).as("Tutor participation belongs to correct exercise").isEqualTo(modelingExercise);
        assertThat(tutorParticipation.getStatus()).as("Tutor participation has correct status").isEqualTo(TutorParticipationStatus.TRAINED);
    }
}
