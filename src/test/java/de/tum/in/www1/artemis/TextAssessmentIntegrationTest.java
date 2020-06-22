package de.tum.in.www1.artemis;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.util.LinkedMultiValueMap;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.tum.in.www1.artemis.domain.AssessmentUpdate;
import de.tum.in.www1.artemis.domain.Complaint;
import de.tum.in.www1.artemis.domain.ComplaintResponse;
import de.tum.in.www1.artemis.domain.Course;
import de.tum.in.www1.artemis.domain.Feedback;
import de.tum.in.www1.artemis.domain.FileUploadExercise;
import de.tum.in.www1.artemis.domain.FileUploadSubmission;
import de.tum.in.www1.artemis.domain.Result;
import de.tum.in.www1.artemis.domain.TextBlock;
import de.tum.in.www1.artemis.domain.TextCluster;
import de.tum.in.www1.artemis.domain.TextExercise;
import de.tum.in.www1.artemis.domain.TextSubmission;
import de.tum.in.www1.artemis.domain.User;
import de.tum.in.www1.artemis.domain.enumeration.AssessmentType;
import de.tum.in.www1.artemis.domain.enumeration.FeedbackType;
import de.tum.in.www1.artemis.domain.enumeration.Language;
import de.tum.in.www1.artemis.domain.participation.Participation;
import de.tum.in.www1.artemis.domain.participation.StudentParticipation;
import de.tum.in.www1.artemis.repository.ComplaintRepository;
import de.tum.in.www1.artemis.repository.CourseRepository;
import de.tum.in.www1.artemis.repository.ExerciseRepository;
import de.tum.in.www1.artemis.repository.FeedbackRepository;
import de.tum.in.www1.artemis.repository.ResultRepository;
import de.tum.in.www1.artemis.repository.StudentParticipationRepository;
import de.tum.in.www1.artemis.repository.TextBlockRepository;
import de.tum.in.www1.artemis.repository.TextClusterRepository;
import de.tum.in.www1.artemis.repository.TextSubmissionRepository;
import de.tum.in.www1.artemis.util.DatabaseUtilService;
import de.tum.in.www1.artemis.util.ModelFactory;
import de.tum.in.www1.artemis.util.RequestUtilService;
import de.tum.in.www1.artemis.util.TextExerciseUtilService;
import de.tum.in.www1.artemis.web.rest.dto.TextAssessmentDTO;
import de.tum.in.www1.artemis.web.rest.dto.TextAssessmentUpdateDTO;

public class TextAssessmentIntegrationTest extends AbstractSpringIntegrationBambooBitbucketJiraTest {

    @Autowired
    CourseRepository courseRepo;

    @Autowired
    ExerciseRepository exerciseRepo;

    @Autowired
    FeedbackRepository feedbackRepository;

    @Autowired
    RequestUtilService request;

    @Autowired
    DatabaseUtilService database;

    @Autowired
    ComplaintRepository complaintRepo;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    private TextClusterRepository textClusterRepository;

    @Autowired
    private TextBlockRepository textBlockRepository;

    @Autowired
    private TextExerciseUtilService textExerciseUtilService;

    @Autowired
    private TextSubmissionRepository textSubmissionRepository;

    @Autowired
    ResultRepository resultRepo;

    @Autowired
    private StudentParticipationRepository studentParticipationRepository;

    private TextExercise textExercise;

    private Course course;

    @BeforeEach
    public void initTestCase() throws Exception {
        database.addUsers(2, 2, 1);
        course = database.addCourseWithOneReleasedTextExercise();
        textExercise = database.findTextExerciseWithTitle(course.getExercises(), "Text");
        textExercise.setAssessmentType(AssessmentType.SEMI_AUTOMATIC);
        exerciseRepo.save(textExercise);
    }

    @AfterEach
    public void tearDown() {
        database.resetDatabase();
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void retrieveParticipationForSubmission_studentHidden() throws Exception {
        TextSubmission textSubmission = ModelFactory.generateTextSubmission("Some text", Language.ENGLISH, false);
        textSubmission = database.addTextSubmission(textExercise, textSubmission, "student1");

        StudentParticipation participationWithoutAssessment = request.get("/api/text-assessments/submission/" + textSubmission.getId(), HttpStatus.OK, StudentParticipation.class);

        assertThat(participationWithoutAssessment).as("participation with submission was found").isNotNull();
        assertThat(participationWithoutAssessment.getSubmissions().iterator().next().getId()).as("participation with correct text submission was found")
                .isEqualTo(textSubmission.getId());
        assertThat(participationWithoutAssessment.getStudent()).as("student of participation is hidden").isEmpty();
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void retrieveParticipationForLockedSubmission() throws Exception {
        TextSubmission textSubmission = ModelFactory.generateTextSubmission("Some text", Language.ENGLISH, false);
        textSubmission = database.addTextSubmissionWithResultAndAssessor(textExercise, textSubmission, "student1", "tutor2");
        Result result = textSubmission.getResult();
        result.setCompletionDate(null); // assessment is still in progress for this test
        resultRepo.save(result);
        StudentParticipation participation = request.get("/api/text-assessments/submission/" + textSubmission.getId(), HttpStatus.BAD_REQUEST, StudentParticipation.class);
        assertThat(participation).as("participation is locked and should not be returned").isNull();
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void retrieveParticipationForNonExistingSubmission() throws Exception {
        StudentParticipation participation = request.get("/api/text-assessments/submission/345395769256365", HttpStatus.BAD_REQUEST, StudentParticipation.class);
        assertThat(participation).as("participation should not be found").isNull();
    }

    @Test
    @WithMockUser(value = "tutor2", roles = "TA")
    public void updateTextAssessmentAfterComplaint_studentHidden() throws Exception {
        TextSubmission textSubmission = ModelFactory.generateTextSubmission("Some text", Language.ENGLISH, true);
        textSubmission = database.addTextSubmissionWithResultAndAssessor(textExercise, textSubmission, "student1", "tutor1");
        Result textAssessment = textSubmission.getResult();
        Complaint complaint = new Complaint().result(textAssessment).complaintText("This is not fair");

        complaintRepo.save(complaint);
        complaint.getResult().setParticipation(null); // Break infinite reference chain

        ComplaintResponse complaintResponse = new ComplaintResponse().complaint(complaint.accepted(false)).responseText("rejected");
        AssessmentUpdate assessmentUpdate = new AssessmentUpdate().feedbacks(new ArrayList<>()).complaintResponse(complaintResponse);

        Result updatedResult = request.putWithResponseBody("/api/text-assessments/text-submissions/" + textSubmission.getId() + "/assessment-after-complaint", assessmentUpdate,
                Result.class, HttpStatus.OK);

        assertThat(updatedResult).as("updated result found").isNotNull();
        assertThat(((StudentParticipation) updatedResult.getParticipation()).getStudent()).as("student of participation is hidden").isEmpty();
    }

    @Test
    @WithMockUser(value = "tutor2", roles = "TA")
    public void updateTextAssessmentAfterComplaint_withTextBlocks() throws Exception {
        // Setup
        TextSubmission textSubmission = ModelFactory.generateTextSubmission("This is Part 1, and this is Part 2. There is also Part 3.", Language.ENGLISH, true);
        textSubmission = database.addTextSubmissionWithResultAndAssessor(textExercise, textSubmission, "student1", "tutor1");
        database.addTextBlocksToTextSubmission(asList(new TextBlock().startIndex(0).endIndex(15).automatic(), new TextBlock().startIndex(16).endIndex(35).automatic(),
                new TextBlock().startIndex(36).endIndex(57).automatic()), textSubmission);

        Result textAssessment = textSubmission.getResult();
        complaintRepo.save(new Complaint().result(textAssessment).complaintText("This is not fair"));

        // Get Text Submission and Complaint
        StudentParticipation participation = request.get("/api/text-assessments/submission/" + textSubmission.getId(), HttpStatus.OK, StudentParticipation.class);
        final Result result = participation.getResults().iterator().next();
        final Complaint complaint = request.get("/api/complaints/result/" + result.getId(), HttpStatus.OK, Complaint.class);

        // Accept Complaint and update Assessment
        ComplaintResponse complaintResponse = new ComplaintResponse().complaint(complaint.accepted(false)).responseText("rejected");
        TextAssessmentUpdateDTO assessmentUpdate = new TextAssessmentUpdateDTO();
        assessmentUpdate.feedbacks(new ArrayList<>()).complaintResponse(complaintResponse);
        assessmentUpdate.setTextBlocks(new ArrayList<>());

        Result updatedResult = request.putWithResponseBody("/api/text-assessments/text-submissions/" + textSubmission.getId() + "/assessment-after-complaint", assessmentUpdate,
                Result.class, HttpStatus.OK);

        assertThat(updatedResult).as("updated result found").isNotNull();
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void saveTextAssessment_studentHidden() throws Exception {
        TextSubmission textSubmission = ModelFactory.generateTextSubmission("Some text", Language.ENGLISH, true);
        database.addTextSubmission(textExercise, textSubmission, "student1");
        exerciseDueDatePassed();

        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("lock", "true");

        TextSubmission submissionWithoutAssessment = request.get("/api/exercises/" + textExercise.getId() + "/text-submission-without-assessment", HttpStatus.OK,
                TextSubmission.class, params);

        final TextAssessmentDTO textAssessmentDTO = new TextAssessmentDTO();
        textAssessmentDTO.setFeedbacks(new ArrayList<>());

        Result result = request.putWithResponseBody("/api/text-assessments/exercise/" + textExercise.getId() + "/result/" + submissionWithoutAssessment.getResult().getId(),
                textAssessmentDTO, Result.class, HttpStatus.OK);

        assertThat(result).as("saved result found").isNotNull();
        assertThat(((StudentParticipation) result.getParticipation()).getStudent()).as("student of participation is hidden").isEmpty();
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void submitTextAssessment_studentHidden() throws Exception {
        TextSubmission textSubmission = ModelFactory.generateTextSubmission("Some text", Language.ENGLISH, true);
        database.addTextSubmission(textExercise, textSubmission, "student1");
        exerciseDueDatePassed();

        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("lock", "true");

        TextSubmission submissionWithoutAssessment = request.get("/api/exercises/" + textExercise.getId() + "/text-submission-without-assessment", HttpStatus.OK,
                TextSubmission.class, params);

        final TextAssessmentDTO textAssessmentDTO = new TextAssessmentDTO();
        textAssessmentDTO.setFeedbacks(new ArrayList<>());
        Result result = request.putWithResponseBody(
                "/api/text-assessments/exercise/" + textExercise.getId() + "/result/" + submissionWithoutAssessment.getResult().getId() + "/submit", textAssessmentDTO,
                Result.class, HttpStatus.OK);

        assertThat(result).as("saved result found").isNotNull();
        assertThat(((StudentParticipation) result.getParticipation()).getStudent()).as("student of participation is hidden").isEmpty();
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void getResult_studentHidden() throws Exception {
        int submissionCount = 5;
        int submissionSize = 4;
        int[] clusterSizes = new int[] { 4, 5, 10, 1 };
        ArrayList<TextBlock> textBlocks = textExerciseUtilService.generateTextBlocks(submissionCount * submissionSize);
        TextExercise textExercise = textExerciseUtilService.createSampleTextExerciseWithSubmissions(course, textBlocks, submissionCount, submissionSize);
        textBlocks.forEach(TextBlock::computeId);
        List<TextCluster> clusters = textExerciseUtilService.addTextBlocksToCluster(textBlocks, clusterSizes, textExercise);
        textClusterRepository.saveAll(clusters);
        textBlockRepository.saveAll(textBlocks);

        StudentParticipation studentParticipation = (StudentParticipation) textSubmissionRepository.findAll().get(0).getParticipation();

        // connect it with a student (!= tutor assessing it)
        User user = database.getUserByLogin("student1");
        studentParticipation.setInitializationDate(ZonedDateTime.now());
        studentParticipation.setParticipant(user);
        studentParticipationRepository.save(studentParticipation);

        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("lock", "true");

        TextSubmission submissionWithoutAssessment = request.get("/api/exercises/" + textExercise.getId() + "/text-submission-without-assessment", HttpStatus.OK,
                TextSubmission.class, params);
        final Result result = submissionWithoutAssessment.getResult();

        assertThat(result).as("saved result found").isNotNull();
        assertThat(((StudentParticipation) submissionWithoutAssessment.getParticipation()).getStudent()).as("student of participation is hidden").isEmpty();
        assertThat(result.getParticipation()).isNull();
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void getParticipationForNonTextExercise() throws Exception {
        FileUploadExercise fileUploadExercise = ModelFactory.generateFileUploadExercise(ZonedDateTime.now().minusDays(1), ZonedDateTime.now().plusDays(1),
                ZonedDateTime.now().plusDays(2), "png,pdf", textExercise.getCourseViaExerciseGroupOrCourseMember());
        exerciseRepo.save(fileUploadExercise);

        FileUploadSubmission fileUploadSubmission = ModelFactory.generateFileUploadSubmission(true);
        database.addFileUploadSubmissionWithResultAndAssessorFeedback(fileUploadExercise, fileUploadSubmission, "student1", "tutor1", new ArrayList<Feedback>());

        final Participation participation = request.get("/api/exercises/" + fileUploadExercise.getId() + "/text-submission-without-assessment", HttpStatus.BAD_REQUEST,
                Participation.class);

        assertThat(participation).as("no result should be returned when exercise is not a text exercise").isNull();
    }

    @Test
    @WithMockUser(value = "student1", roles = "USER")
    public void getDataForTextEditor_assessorHidden() throws Exception {
        TextSubmission textSubmission = ModelFactory.generateTextSubmission("Some text", Language.ENGLISH, true);
        textSubmission = database.addTextSubmissionWithResultAndAssessor(textExercise, textSubmission, "student1", "tutor1");

        Participation participation = request.get("/api/text-editor/" + textSubmission.getParticipation().getId(), HttpStatus.OK, Participation.class);

        assertThat(participation).as("participation found").isNotNull();
        assertThat(participation.getResults().iterator().next()).as("result found").isNotNull();
        assertThat(participation.getResults().iterator().next().getAssessor()).as("assessor of participation is hidden").isNull();
    }

    @Test
    @WithMockUser(value = "student1", roles = "USER")
    public void getDataForTextEditor_hasTextBlocks() throws Exception {
        TextSubmission textSubmission = ModelFactory.generateTextSubmission("Some text", Language.ENGLISH, true);
        ArrayList<TextBlock> textBlocks = textExerciseUtilService.generateTextBlocks(1);
        textSubmission = database.addTextSubmissionWithResultAndAssessor(textExercise, textSubmission, "student1", "tutor1");
        database.addTextBlocksToTextSubmission(textBlocks, textSubmission);

        Participation participation = request.get("/api/text-editor/" + textSubmission.getParticipation().getId(), HttpStatus.OK, Participation.class);

        final TextSubmission submission = (TextSubmission) participation.getResults().iterator().next().getSubmission();
        assertThat(submission.getBlocks()).isNotNull();
        assertThat(submission.getBlocks()).isNotEmpty();
    }

    @Test
    @WithMockUser(value = "student2", roles = "USER")
    public void getDataForTextEditor_asOtherStudent() throws Exception {
        TextSubmission textSubmission = ModelFactory.generateTextSubmission("Some text", Language.ENGLISH, true);
        textSubmission = database.addTextSubmissionWithResultAndAssessor(textExercise, textSubmission, "student1", "tutor1");
        request.get("/api/text-editor/" + textSubmission.getParticipation().getId(), HttpStatus.FORBIDDEN, Participation.class);
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void getDataForTextEditor_studentHidden() throws Exception {
        TextSubmission textSubmission = ModelFactory.generateTextSubmission("Some text", Language.ENGLISH, true);
        textSubmission = database.addTextSubmissionWithResultAndAssessor(textExercise, textSubmission, "student1", "tutor1");

        StudentParticipation participation = request.get("/api/text-editor/" + textSubmission.getParticipation().getId(), HttpStatus.OK, StudentParticipation.class);

        assertThat(participation).as("participation found").isNotNull();
        assertThat(participation.getResults().iterator().next()).as("result found").isNotNull();
        assertThat(participation.getStudent()).as("student of participation is hidden").isEmpty();
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void getDataForTextEditor_submissionWithoutResult() throws Exception {
        TextSubmission textSubmission = ModelFactory.generateTextSubmission("Some text", Language.ENGLISH, true);
        textSubmission = database.addTextSubmission(textExercise, textSubmission, "student1");
        request.get("/api/text-editor/" + textSubmission.getParticipation().getId(), HttpStatus.OK, StudentParticipation.class);
    }

    private void getExampleResultForTutor(HttpStatus expectedStatus, boolean isExample) throws Exception {
        TextSubmission textSubmission = ModelFactory.generateTextSubmission("Some text", Language.ENGLISH, true);
        textSubmission.setExampleSubmission(isExample);
        textSubmission = database.addTextSubmissionWithResultAndAssessor(textExercise, textSubmission, "student1", "instructor1");

        Result result = request.get("/api/text-assessments/exercise/" + textExercise.getId() + "/submission/" + textSubmission.getId() + "/example-result", expectedStatus,
                Result.class);

        if (expectedStatus == HttpStatus.OK) {
            assertThat(result).as("result found").isNotNull();
            assertThat(result.getSubmission().getId()).as("result for correct submission").isEqualTo(textSubmission.getId());
        }
    }

    @Test
    @WithMockUser(value = "student1", roles = "USER")
    public void getExampleResultForTutorAsStudent() throws Exception {
        getExampleResultForTutor(HttpStatus.FORBIDDEN, true);
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void getExampleResultForTutorAsTutor() throws Exception {
        getExampleResultForTutor(HttpStatus.OK, true);
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void getExampleResultForNonExampleSubmissionAsTutor() throws Exception {
        getExampleResultForTutor(HttpStatus.NOT_FOUND, false);
    }

    private void cancelAssessment(HttpStatus expectedStatus) throws Exception {
        TextSubmission textSubmission = ModelFactory.generateTextSubmission("Some text", Language.ENGLISH, true);
        textSubmission = database.addTextSubmissionWithResultAndAssessor(textExercise, textSubmission, "student1", "tutor1");
        database.addSampleFeedbackToResults(textSubmission.getResult());
        request.put("/api/text-assessments/exercise/" + textExercise.getId() + "/submission/" + textSubmission.getId() + "/cancel-assessment", null, expectedStatus);
    }

    @Test
    @WithMockUser(value = "student1", roles = "USER")
    public void cancelOwnAssessmentAsStudent() throws Exception {
        cancelAssessment(HttpStatus.FORBIDDEN);
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void cancelOwnAssessmentAsTutor() throws Exception {
        cancelAssessment(HttpStatus.OK);
    }

    @Test
    @WithMockUser(value = "tutor2", roles = "TA")
    public void cancelAssessmentOfOtherTutorAsTutor() throws Exception {
        cancelAssessment(HttpStatus.FORBIDDEN);
    }

    @Test
    @WithMockUser(value = "instructor1", roles = "INSTRUCTOR")
    public void cancelAssessmentOfOtherTutorAsInstructor() throws Exception {
        cancelAssessment(HttpStatus.OK);
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void cancelAssessment_wrongSubmissionId() throws Exception {
        request.put("/api/text-assessments/exercise/" + textExercise.getId() + "/submission/100/cancel-assessment", null, HttpStatus.NOT_FOUND);
    }

    @Test
    @WithMockUser(value = "tutor2", roles = "TA")
    public void testOverrideAssessment_saveOtherTutorForbidden() throws Exception {
        overrideAssessment("student1", "tutor1", HttpStatus.FORBIDDEN, "false", true);
    }

    @Test
    @WithMockUser(value = "instructor1", roles = "INSTRUCTOR")
    public void testOverrideAssessment_saveInstructorPossible() throws Exception {
        overrideAssessment("student1", "tutor1", HttpStatus.OK, "false", true);
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void testOverrideAssessment_saveSameTutorPossible() throws Exception {
        overrideAssessment("student1", "tutor1", HttpStatus.OK, "false", true);
    }

    @Test
    @WithMockUser(value = "tutor2", roles = "TA")
    public void testOverrideAssessment_submitOtherTutorForbidden() throws Exception {
        overrideAssessment("student1", "tutor1", HttpStatus.FORBIDDEN, "true", true);
    }

    @Test
    @WithMockUser(value = "instructor1", roles = "INSTRUCTOR")
    public void testOverrideAssessment_submitInstructorPossible() throws Exception {
        overrideAssessment("student1", "tutor1", HttpStatus.OK, "true", true);
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void testOverrideAssessment_submitSameTutorPossible() throws Exception {
        overrideAssessment("student1", "tutor1", HttpStatus.OK, "true", true);
    }

    @Test
    @WithMockUser(value = "tutor2", roles = "TA")
    public void testOverrideAssessment_saveOtherTutorAfterAssessmentDueDateForbidden() throws Exception {
        assessmentDueDatePassed();
        overrideAssessment("student1", "tutor1", HttpStatus.FORBIDDEN, "false", true);
    }

    @Test
    @WithMockUser(value = "instructor1", roles = "INSTRUCTOR")
    public void testOverrideAssessment_saveInstructorAfterAssessmentDueDatePossible() throws Exception {
        assessmentDueDatePassed();
        overrideAssessment("student1", "tutor1", HttpStatus.OK, "false", true);
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void testOverrideAssessment_saveSameTutorAfterAssessmentDueDateForbidden() throws Exception {
        assessmentDueDatePassed();
        overrideAssessment("student1", "tutor1", HttpStatus.FORBIDDEN, "false", true);
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void testOverrideAssessment_saveSameTutorAfterAssessmentDueDatePossible() throws Exception {
        assessmentDueDatePassed();
        // should be possible because the original result was not yet submitted
        overrideAssessment("student1", "tutor1", HttpStatus.OK, "false", false);
    }

    @Test
    @WithMockUser(value = "tutor2", roles = "TA")
    public void testOverrideAssessment_submitOtherTutorAfterAssessmentDueDateForbidden() throws Exception {
        assessmentDueDatePassed();
        overrideAssessment("student1", "tutor1", HttpStatus.FORBIDDEN, "true", true);
    }

    @Test
    @WithMockUser(value = "instructor1", roles = "INSTRUCTOR")
    public void testOverrideAssessment_submitInstructorAfterAssessmentDueDatePossible() throws Exception {
        assessmentDueDatePassed();
        overrideAssessment("student1", "tutor1", HttpStatus.OK, "true", true);
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void testOverrideAssessment_submitSameTutorAfterAssessmentDueDateForbidden() throws Exception {
        assessmentDueDatePassed();
        overrideAssessment("student1", "tutor1", HttpStatus.FORBIDDEN, "true", true);
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void testOverrideAssessment_submitSameTutorAfterAssessmentDueDatePossible() throws Exception {
        assessmentDueDatePassed();
        // should be possible because the original result was not yet submitted
        overrideAssessment("student1", "tutor1", HttpStatus.OK, "true", false);
    }

    private void exerciseDueDatePassed() {
        database.updateExerciseDueDate(textExercise.getId(), ZonedDateTime.now().minusHours(2));
    }

    private void assessmentDueDatePassed() {
        database.updateAssessmentDueDate(textExercise.getId(), ZonedDateTime.now().minusSeconds(10));
    }

    private void overrideAssessment(String student, String originalAssessor, HttpStatus httpStatus, String submit, boolean originalAssessmentSubmitted) throws Exception {
        TextSubmission textSubmission = ModelFactory.generateTextSubmission("Test123", Language.ENGLISH, true);
        textSubmission = database.addTextSubmissionWithResultAndAssessor(textExercise, textSubmission, student, originalAssessor);
        textSubmission.getResult().setCompletionDate(originalAssessmentSubmitted ? ZonedDateTime.now() : null);
        resultRepo.save(textSubmission.getResult());
        var params = new LinkedMultiValueMap<String, String>();
        params.add("submit", submit);
        List<Feedback> feedbacks = ModelFactory.generateFeedback();
        var path = "/api/text-assessments/exercise/" + textExercise.getId() + "/result/" + textSubmission.getResult().getId();
        if (submit.equals("true")) {
            path = path + "/submit";
        }
        var body = new TextAssessmentDTO(feedbacks);
        request.putWithResponseBodyAndParams(path, body, Result.class, httpStatus, params);
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void testTextBlocksAreConsistentWhenOpeningSameAssessmentTwiceWithAtheneEnabled() throws Exception {
        TextSubmission textSubmission = ModelFactory.generateTextSubmission("This is Part 1, and this is Part 2. There is also Part 3.", Language.ENGLISH, true);
        database.addTextSubmission(textExercise, textSubmission, "student1");
        exerciseDueDatePassed();

        var blocks = asList(new TextBlock().startIndex(0).endIndex(15).automatic(), new TextBlock().startIndex(16).endIndex(35).automatic(),
                new TextBlock().startIndex(36).endIndex(57).automatic());
        database.addTextBlocksToTextSubmission(blocks, textSubmission);

        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("lock", "true");

        TextSubmission submission1stRequest = request.get("/api/exercises/" + textExercise.getId() + "/text-submission-without-assessment", HttpStatus.OK, TextSubmission.class,
                params);

        var blocksFrom1stRequest = submission1stRequest.getBlocks();
        assertThat(blocksFrom1stRequest.toArray()).containsExactlyInAnyOrder(blocks.toArray());

        final TextAssessmentDTO textAssessmentDTO = new TextAssessmentDTO();
        textAssessmentDTO.setFeedbacks(asList(new Feedback().detailText("Test").credits(1d).reference(blocksFrom1stRequest.get(0).getId()).type(FeedbackType.MANUAL)));
        textAssessmentDTO.setTextBlocks(blocksFrom1stRequest);
        Result result = request.putWithResponseBody("/api/text-assessments/exercise/" + textExercise.getId() + "/result/" + submission1stRequest.getResult().getId() + "/submit",
                textAssessmentDTO, Result.class, HttpStatus.OK);

        Participation participation2ndRequest = request.get("/api/text-assessments/submission/" + textSubmission.getId(), HttpStatus.OK, Participation.class, params);
        TextSubmission submission2ndRequest = (TextSubmission) (participation2ndRequest).getSubmissions().iterator().next();
        var blocksFrom2ndRequest = submission2ndRequest.getBlocks();
        assertThat(blocksFrom2ndRequest.toArray()).containsExactlyInAnyOrder(blocks.toArray());
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void checkTextSubmissionWithoutAssessmentAndRetrieveParticipationForSubmissionReturnSameBlocksAndFeedback() throws Exception {
        List<TextSubmission> textSubmissions = prepareTextSubmissionsWithFeedbackForAutomaticFeedback();

        LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("lock", "true");
        TextSubmission textSubmissionWithoutAssessment = request.get("/api/exercises/" + textExercise.getId() + "/text-submission-without-assessment", HttpStatus.OK,
                TextSubmission.class, parameters);

        request.put("/api/text-assessments/exercise/" + textExercise.getId() + "/submission/" + textSubmissions.get(0).getId() + "/cancel-assessment", null, HttpStatus.OK);

        LinkedMultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("lock", "true");
        Participation participation = request.get("/api/text-assessments/submission/" + textSubmissions.get(0).getId(), HttpStatus.OK, Participation.class, params);
        final TextSubmission submissionFromParticipation = (TextSubmission) participation.getSubmissions().toArray()[0];
        final Result resultFromParticipation = (Result) participation.getResults().toArray()[0];

        assertThat(textSubmissionWithoutAssessment.getId()).isEqualTo(submissionFromParticipation.getId());
        assertThat(Arrays.equals(textSubmissionWithoutAssessment.getBlocks().toArray(), submissionFromParticipation.getBlocks().toArray())).isTrue();
        final Feedback feedbackFromSubmissionWithoutAssessment = textSubmissionWithoutAssessment.getResult().getFeedbacks().get(0);
        final Feedback feedbackFromParticipation = resultFromParticipation.getFeedbacks().get(0);
        assertThat(feedbackFromSubmissionWithoutAssessment.getCredits()).isEqualTo(feedbackFromParticipation.getCredits());
        assertThat(feedbackFromSubmissionWithoutAssessment.getDetailText()).isEqualTo(feedbackFromParticipation.getDetailText());
        assertThat(feedbackFromSubmissionWithoutAssessment.getType()).isEqualTo(feedbackFromParticipation.getType());
    }

    @NotNull
    private List<TextSubmission> prepareTextSubmissionsWithFeedbackForAutomaticFeedback() {
        TextSubmission textSubmission1 = ModelFactory.generateTextSubmission("This is Part 1, and this is Part 2. There is also Part 3.", Language.ENGLISH, true);
        TextSubmission textSubmission2 = ModelFactory.generateTextSubmission("This is another Submission.", Language.ENGLISH, true);
        var textSubmissions = asList(textSubmission1, textSubmission2);
        database.addTextSubmission(textExercise, textSubmission1, "student1");
        database.addTextSubmission(textExercise, textSubmission2, "student2");
        exerciseDueDatePassed();

        final TextCluster cluster = new TextCluster().exercise(textExercise);
        textClusterRepository.save(cluster);

        final TextBlock textBlockSubmission1 = new TextBlock().startIndex(0).endIndex(15).automatic().cluster(cluster);
        final TextBlock textBlockSubmission2 = new TextBlock().startIndex(0).endIndex(27).automatic().cluster(cluster);

        cluster.blocks(asList(textBlockSubmission1, textBlockSubmission2)).distanceMatrix(new double[][] { { 0.1, 0.1 }, { 0.1, 0.1 } });

        database.addTextBlocksToTextSubmission(
                asList(textBlockSubmission1, new TextBlock().startIndex(16).endIndex(35).automatic(), new TextBlock().startIndex(36).endIndex(57).automatic()), textSubmission1);

        database.addTextBlocksToTextSubmission(asList(textBlockSubmission2), textSubmission2);

        textClusterRepository.save(cluster);

        final Feedback feedback = new Feedback().detailText("Foo Bar.").credits(2d).reference(textBlockSubmission2.getId());
        database.addTextSubmissionWithResultAndAssessorAndFeedbacks(textExercise, textSubmission2, "student2", "tutor1", asList(feedback));
        feedbackRepository.save(feedback);
        return textSubmissions;
    }

    @Test
    @WithMockUser(value = "tutor1", roles = "TA")
    public void checkTextBlockSavePreservesClusteringInformation() throws Exception {
        List<TextSubmission> textSubmissions = prepareTextSubmissionsWithFeedbackForAutomaticFeedback();
        final Map<String, TextBlock> blocksSubmission1 = textSubmissions.get(0).getBlocks().stream().collect(Collectors.toMap(TextBlock::getId, block -> block));

        LinkedMultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("lock", "true");
        TextSubmission textSubmissionWithoutAssessment = request.get("/api/exercises/" + textExercise.getId() + "/text-submission-without-assessment", HttpStatus.OK,
                TextSubmission.class, parameters);

        textSubmissionWithoutAssessment.getBlocks()
                .forEach(block -> assertThat(block).isEqualToIgnoringGivenFields(blocksSubmission1.get(block.getId()), "positionInCluster", "submission", "cluster"));

        textBlockRepository.findAllWithEagerClusterBySubmissionId(textSubmissionWithoutAssessment.getId())
                .forEach(block -> assertThat(block).isEqualToComparingFieldByField(blocksSubmission1.get(block.getId())));

        final List<TextBlock> newTextBlocksToSimulateAngularSerialization = textSubmissionWithoutAssessment.getBlocks().stream().map(oldBlock -> {
            var newBlock = new TextBlock();
            newBlock.setText(oldBlock.getText());
            newBlock.setStartIndex(oldBlock.getStartIndex());
            newBlock.setEndIndex(oldBlock.getEndIndex());
            newBlock.setId(oldBlock.getId());
            return newBlock;
        }).collect(Collectors.toList());

        final TextAssessmentDTO dto = new TextAssessmentDTO();
        dto.setTextBlocks(newTextBlocksToSimulateAngularSerialization);
        dto.setFeedbacks(new ArrayList<>());

        Result result = request.putWithResponseBody("/api/text-assessments/exercise/" + textExercise.getId() + "/result/" + textSubmissionWithoutAssessment.getResult().getId(),
                dto, Result.class, HttpStatus.OK);

        textBlockRepository.findAllWithEagerClusterBySubmissionId(textSubmissionWithoutAssessment.getId())
                .forEach(block -> assertThat(block).isEqualToComparingFieldByField(blocksSubmission1.get(block.getId())));
    }
}
