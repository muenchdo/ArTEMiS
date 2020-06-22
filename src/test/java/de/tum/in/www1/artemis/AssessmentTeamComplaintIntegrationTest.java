package de.tum.in.www1.artemis;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.tum.in.www1.artemis.domain.*;
import de.tum.in.www1.artemis.domain.enumeration.ComplaintType;
import de.tum.in.www1.artemis.domain.enumeration.ExerciseMode;
import de.tum.in.www1.artemis.domain.enumeration.FeedbackType;
import de.tum.in.www1.artemis.domain.modeling.ModelingExercise;
import de.tum.in.www1.artemis.domain.modeling.ModelingSubmission;
import de.tum.in.www1.artemis.domain.participation.StudentParticipation;
import de.tum.in.www1.artemis.repository.*;
import de.tum.in.www1.artemis.util.DatabaseUtilService;
import de.tum.in.www1.artemis.util.ModelFactory;
import de.tum.in.www1.artemis.util.RequestUtilService;

public class AssessmentTeamComplaintIntegrationTest extends AbstractSpringIntegrationBambooBitbucketJiraTest {

    @Autowired
    ExerciseRepository exerciseRepo;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    RequestUtilService request;

    @Autowired
    DatabaseUtilService database;

    @Autowired
    ResultRepository resultRepo;

    @Autowired
    ComplaintRepository complaintRepo;

    @Autowired
    ComplaintResponseRepository complaintResponseRepo;

    @Autowired
    ObjectMapper mapper;

    private ModelingExercise modelingExercise;

    private ModelingSubmission modelingSubmission;

    private Result modelingAssessment;

    private Complaint complaint;

    private Complaint moreFeedbackRequest;

    private Course course;

    private Team team;

    private final String resourceUrl = "/api/complaints";

    @BeforeEach
    public void initTestCase() throws Exception {
        database.addUsers(1, 2, 1);
        // Initialize with 3 max team complaints and 7 days max complaint deadline
        course = database.addCourseWithOneModelingExercise();
        modelingExercise = (ModelingExercise) course.getExercises().iterator().next();
        modelingExercise = (ModelingExercise) exerciseRepo.save(modelingExercise.mode(ExerciseMode.TEAM));
        team = database.addTeamForExercise(modelingExercise, database.getUserByLogin("tutor1"));
        saveModelingSubmissionAndAssessment();
        complaint = new Complaint().result(modelingAssessment).complaintText("This is not fair").complaintType(ComplaintType.COMPLAINT);
        moreFeedbackRequest = new Complaint().result(modelingAssessment).complaintText("Please explain").complaintType(ComplaintType.MORE_FEEDBACK);
    }

    @AfterEach
    public void tearDown() {
        database.resetDatabase();
    }

    @Test
    @WithMockUser(username = "team1student1")
    public void submitComplaintAboutModellingAssessment_complaintLimitNotReached() throws Exception {
        // 2 complaints are allowed, the course is created with 3 max team complaints
        database.addTeamComplaints(team, modelingAssessment.getParticipation(), 2, ComplaintType.COMPLAINT);

        request.post(resourceUrl, complaint, HttpStatus.CREATED);

        assertThat(complaintRepo.findByResult_Id(modelingAssessment.getId())).as("complaint is saved").isPresent();
        Result storedResult = resultRepo.findByIdWithEagerFeedbacksAndAssessor(modelingAssessment.getId()).get();
        assertThat(storedResult.hasComplaint()).as("hasComplaint flag of result is true").isTrue();
    }

    @Test
    @WithMockUser(username = "team1student1")
    public void submitComplaintAboutModelingAssessment_complaintLimitReached() throws Exception {
        database.addTeamComplaints(team, modelingAssessment.getParticipation(), 3, ComplaintType.COMPLAINT);

        request.post(resourceUrl, complaint, HttpStatus.BAD_REQUEST);

        assertThat(complaintRepo.findByResult_Id(modelingAssessment.getId())).as("complaint is not saved").isNotPresent();
        Result storedResult = resultRepo.findByIdWithEagerFeedbacksAndAssessor(modelingAssessment.getId()).get();
        assertThat(storedResult.hasComplaint()).as("hasComplaint flag of result is false").isFalse();
    }

    @Test
    @WithMockUser(username = "team1student1")
    public void requestMoreFeedbackAboutModelingAssessment_noLimit() throws Exception {
        database.addTeamComplaints(team, modelingAssessment.getParticipation(), 5, ComplaintType.MORE_FEEDBACK);

        request.post(resourceUrl, complaint, HttpStatus.CREATED);

        assertThat(complaintRepo.findByResult_Id(modelingAssessment.getId())).as("complaint is saved").isPresent();
        Result storedResult = resultRepo.findByIdWithEagerFeedbacksAndAssessor(modelingAssessment.getId()).get();
        assertThat(storedResult.hasComplaint()).as("hasComplaint flag of result is true").isTrue();

        // Only one complaint is possible for exercise regardless of its type
        request.post(resourceUrl, moreFeedbackRequest, HttpStatus.BAD_REQUEST);
        assertThat(complaintRepo.findByResult_Id(modelingAssessment.getId()).get().getComplaintType() == ComplaintType.MORE_FEEDBACK).as("more feedback request is not saved")
                .isFalse();
    }

    @Test
    @WithMockUser(username = "team1student1")
    public void submitComplaintAboutModelingAssessment_validDeadline() throws Exception {
        // Mock object initialized with 2 weeks deadline. One week after result date is fine.
        database.updateAssessmentDueDate(modelingExercise.getId(), ZonedDateTime.now().minusWeeks(1));
        database.updateResultCompletionDate(modelingAssessment.getId(), ZonedDateTime.now().minusWeeks(1));

        request.post(resourceUrl, complaint, HttpStatus.CREATED);

        assertThat(complaintRepo.findByResult_Id(modelingAssessment.getId())).as("complaint is saved").isPresent();
        Result storedResult = resultRepo.findByIdWithEagerFeedbacksAndAssessor(modelingAssessment.getId()).get();
        assertThat(storedResult.hasComplaint()).as("hasComplaint flag of result is true").isTrue();
    }

    @Test
    @WithMockUser(username = "team1student1")
    public void submitComplaintAboutModelingAssessment_assessmentTooOld() throws Exception {
        // 3 weeks is already past the deadline
        database.updateAssessmentDueDate(modelingExercise.getId(), ZonedDateTime.now().minusWeeks(3));
        database.updateResultCompletionDate(modelingAssessment.getId(), ZonedDateTime.now().minusWeeks(3));

        request.post(resourceUrl, complaint, HttpStatus.BAD_REQUEST);

        assertThat(complaintRepo.findByResult_Id(modelingAssessment.getId())).as("complaint is not saved").isNotPresent();
        Result storedResult = resultRepo.findByIdWithEagerFeedbacksAndAssessor(modelingAssessment.getId()).get();
        assertThat(storedResult.hasComplaint()).as("hasComplaint flag of result is false").isFalse();
    }

    @Test
    @WithMockUser(username = "tutor1", roles = "TA")
    public void submitComplaintResponse_rejectComplaint() throws Exception {
        complaintRepo.save(complaint);

        ComplaintResponse complaintResponse = new ComplaintResponse().complaint(complaint.accepted(false)).responseText("rejected");
        request.post("/api/complaint-responses", complaintResponse, HttpStatus.CREATED);
        assertThat(complaintResponse.getComplaint().getParticipant()).isNull();

        Complaint storedComplaint = complaintRepo.findByResult_Id(modelingAssessment.getId()).get();
        assertThat(storedComplaint.isAccepted()).as("complaint is not accepted").isFalse();
        Result storedResult = resultRepo.findWithEagerSubmissionAndFeedbackAndAssessorById(modelingAssessment.getId()).get();
        checkFeedbackCorrectlyStored(modelingAssessment.getFeedbacks(), storedResult.getFeedbacks());
        assertThat(storedResult).as("only feedbacks are changed in the result").isEqualToIgnoringGivenFields(modelingAssessment, "feedbacks");
    }

    @Test
    @WithMockUser(username = "tutor2", roles = "TA")
    public void submitComplaintResponse_rejectComplaint_asOtherTutor_forbidden() throws Exception {
        complaintRepo.save(complaint);

        ComplaintResponse complaintResponse = new ComplaintResponse().complaint(complaint.accepted(false)).responseText("rejected");
        request.post("/api/complaint-responses", complaintResponse, HttpStatus.FORBIDDEN);
    }

    @Test
    @WithMockUser(username = "tutor1", roles = "TA")
    public void submitComplaintResponse_updateAssessment() throws Exception {
        complaint = complaintRepo.save(complaint);

        List<Feedback> feedback = database.loadAssessmentFomResources("test-data/model-assessment/assessment.54727.json");
        ComplaintResponse complaintResponse = new ComplaintResponse().complaint(complaint.accepted(true)).responseText("accepted");
        AssessmentUpdate assessmentUpdate = new AssessmentUpdate().feedbacks(feedback).complaintResponse(complaintResponse);
        Result receivedResult = request.putWithResponseBody("/api/modeling-submissions/" + modelingSubmission.getId() + "/assessment-after-complaint", assessmentUpdate,
                Result.class, HttpStatus.OK);

        assertThat(((StudentParticipation) receivedResult.getParticipation()).getStudent()).as("student is hidden in response").isEmpty();
        Complaint storedComplaint = complaintRepo.findByResult_Id(modelingAssessment.getId()).get();
        assertThat(storedComplaint.isAccepted()).as("complaint is accepted").isTrue();
        Result resultBeforeComplaint = mapper.readValue(storedComplaint.getResultBeforeComplaint(), Result.class);
        // set dates to UTC and round to milliseconds for comparison
        resultBeforeComplaint.setCompletionDate(ZonedDateTime.ofInstant(resultBeforeComplaint.getCompletionDate().truncatedTo(ChronoUnit.MILLIS).toInstant(), ZoneId.of("UTC")));
        modelingAssessment.setCompletionDate(ZonedDateTime.ofInstant(modelingAssessment.getCompletionDate().truncatedTo(ChronoUnit.MILLIS).toInstant(), ZoneId.of("UTC")));
        assertThat(resultBeforeComplaint).as("result before complaint is correctly stored").isEqualToIgnoringGivenFields(modelingAssessment, "participation", "submission");
        Result storedResult = resultRepo.findByIdWithEagerFeedbacksAndAssessor(modelingAssessment.getId()).get();
        checkFeedbackCorrectlyStored(feedback, storedResult.getFeedbacks());
        assertThat(storedResult.getAssessor()).as("assessor is still the original one").isEqualTo(modelingAssessment.getAssessor());
    }

    @Test
    @WithMockUser(username = "tutor2", roles = "TA")
    public void submitComplaintResponse_updateAssessment_asOtherTutor_forbidden() throws Exception {
        complaint = complaintRepo.save(complaint);

        List<Feedback> feedback = database.loadAssessmentFomResources("test-data/model-assessment/assessment.54727.json");
        ComplaintResponse complaintResponse = new ComplaintResponse().complaint(complaint.accepted(true)).responseText("accepted");
        AssessmentUpdate assessmentUpdate = new AssessmentUpdate().feedbacks(feedback).complaintResponse(complaintResponse);
        request.putWithResponseBody("/api/modeling-submissions/" + modelingSubmission.getId() + "/assessment-after-complaint", assessmentUpdate, Result.class,
                HttpStatus.FORBIDDEN);
    }

    @Test
    @WithMockUser(username = "student1")
    public void getComplaintByResultId_studentAndNotPartOfTeam_forbidden() throws Exception {
        complaint.setParticipant(team);
        complaintRepo.save(complaint);

        request.get("/api/complaints/result/" + complaint.getResult().getId(), HttpStatus.FORBIDDEN, Complaint.class);
    }

    @Test
    @WithMockUser(username = "student1")
    public void getComplaintResponseByComplaintId_studentNotPartOfTeam_forbidden() throws Exception {
        complaint.setParticipant(team);
        complaintRepo.save(complaint);

        ComplaintResponse complaintResponse = new ComplaintResponse().complaint(complaint.accepted(false)).responseText("rejected").reviewer(database.getUserByLogin("tutor1"));
        complaintResponseRepo.save(complaintResponse);

        request.get("/api/complaint-responses/complaint/" + complaint.getId(), HttpStatus.FORBIDDEN, ComplaintResponse.class);
    }

    @Test
    @WithMockUser(username = "tutor1", roles = "TA")
    public void getComplaintById() throws Exception {
        complaintRepo.save(complaint);
        request.get("/api/complaints/" + complaint.getId(), HttpStatus.OK, Complaint.class);
    }

    private void checkFeedbackCorrectlyStored(List<Feedback> sentFeedback, List<Feedback> storedFeedback) {
        assertThat(sentFeedback.size()).as("contains the same amount of feedback").isEqualTo(storedFeedback.size());
        Result storedFeedbackResult = new Result();
        Result sentFeedbackResult = new Result();
        storedFeedbackResult.setFeedbacks(storedFeedback);
        sentFeedbackResult.setFeedbacks(sentFeedback);
        storedFeedbackResult.evaluateFeedback(20);
        sentFeedbackResult.evaluateFeedback(20);
        assertThat(storedFeedbackResult.getScore()).as("stored feedback evaluates to the same score as sent feedback").isEqualTo(sentFeedbackResult.getScore());
        storedFeedback.forEach(feedback -> {
            assertThat(feedback.getType()).as("type has been set to MANUAL").isEqualTo(FeedbackType.MANUAL);
        });
    }

    private void saveModelingSubmissionAndAssessment() throws Exception {
        modelingSubmission = ModelFactory.generateModelingSubmission(database.loadFileFromResources("test-data/model-submission/model.54727.json"), true);
        modelingSubmission = database.addModelingTeamSubmission(modelingExercise, modelingSubmission, team);
        modelingAssessment = database.addModelingAssessmentForSubmission(modelingExercise, modelingSubmission, "test-data/model-assessment/assessment.54727.v2.json", "tutor1",
                true);
    }

    @Test
    @WithMockUser(username = "tutor1", roles = "TA")
    public void getNumberOfAllowedTeamComplaintsInCourse() throws Exception {
        complaint.setParticipant(team);
        complaintRepo.save(complaint);
        Long nrOfAllowedComplaints = request.get("/api/courses/" + modelingExercise.getCourseViaExerciseGroupOrCourseMember().getId() + "/allowed-complaints?isTeamMode=true",
                HttpStatus.OK, Long.class);
        assertThat(nrOfAllowedComplaints.intValue()).isEqualTo(course.getMaxTeamComplaints());
    }

}
