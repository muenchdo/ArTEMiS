package de.tum.in.www1.artemis.web.rest;

import static de.tum.in.www1.artemis.web.rest.util.ResponseUtil.badRequest;
import static de.tum.in.www1.artemis.web.rest.util.ResponseUtil.forbidden;
import static de.tum.in.www1.artemis.web.rest.util.ResponseUtil.notFound;

import java.security.Principal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.tum.in.www1.artemis.domain.Course;
import de.tum.in.www1.artemis.domain.Exercise;
import de.tum.in.www1.artemis.domain.GradingCriterion;
import de.tum.in.www1.artemis.domain.TextExercise;
import de.tum.in.www1.artemis.domain.TextSubmission;
import de.tum.in.www1.artemis.domain.User;
import de.tum.in.www1.artemis.domain.participation.StudentParticipation;
import de.tum.in.www1.artemis.repository.TextSubmissionRepository;
import de.tum.in.www1.artemis.service.AuthorizationCheckService;
import de.tum.in.www1.artemis.service.CourseService;
import de.tum.in.www1.artemis.service.ExerciseService;
import de.tum.in.www1.artemis.service.GradingCriterionService;
import de.tum.in.www1.artemis.service.TextAssessmentService;
import de.tum.in.www1.artemis.service.TextExerciseService;
import de.tum.in.www1.artemis.service.TextSubmissionService;
import de.tum.in.www1.artemis.service.UserService;
import de.tum.in.www1.artemis.service.scheduled.TextClusteringScheduleService;
import de.tum.in.www1.artemis.web.rest.errors.AccessForbiddenException;
import de.tum.in.www1.artemis.web.rest.errors.BadRequestAlertException;
import io.github.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing TextSubmission.
 */
@RestController
@RequestMapping("/api")
public class TextSubmissionResource {

    private static final String ENTITY_NAME = "textSubmission";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final Logger log = LoggerFactory.getLogger(TextSubmissionResource.class);

    private final TextSubmissionRepository textSubmissionRepository;

    private final ExerciseService exerciseService;

    private final TextExerciseService textExerciseService;

    private final CourseService courseService;

    private final AuthorizationCheckService authorizationCheckService;

    private final TextSubmissionService textSubmissionService;

    private final TextAssessmentService textAssessmentService;

    private final UserService userService;

    private final GradingCriterionService gradingCriterionService;

    private final Optional<TextClusteringScheduleService> textClusteringScheduleService;

    public TextSubmissionResource(TextSubmissionRepository textSubmissionRepository, ExerciseService exerciseService, TextExerciseService textExerciseService,
            CourseService courseService, AuthorizationCheckService authorizationCheckService, TextSubmissionService textSubmissionService, UserService userService,
            GradingCriterionService gradingCriterionService, TextAssessmentService textAssessmentService, Optional<TextClusteringScheduleService> textClusteringScheduleService) {
        this.textSubmissionRepository = textSubmissionRepository;
        this.exerciseService = exerciseService;
        this.textExerciseService = textExerciseService;
        this.courseService = courseService;
        this.authorizationCheckService = authorizationCheckService;
        this.textSubmissionService = textSubmissionService;
        this.userService = userService;
        this.gradingCriterionService = gradingCriterionService;
        this.textClusteringScheduleService = textClusteringScheduleService;
        this.textAssessmentService = textAssessmentService;
    }

    /**
     * POST /exercises/{exerciseId}/text-submissions : Create a new textSubmission. This is called when a student saves his/her answer
     *
     * @param exerciseId     the id of the exercise for which to init a participation
     * @param principal      the current user principal
     * @param textSubmission the textSubmission to create
     * @return the ResponseEntity with status 200 (OK) and the Result as its body, or with status 4xx if the request is invalid
     */
    @PostMapping("/exercises/{exerciseId}/text-submissions")
    @PreAuthorize("hasAnyRole('USER', 'TA', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<TextSubmission> createTextSubmission(@PathVariable Long exerciseId, Principal principal, @RequestBody TextSubmission textSubmission) {
        log.debug("REST request to save TextSubmission : {}", textSubmission);
        if (textSubmission.getId() != null) {
            throw new BadRequestAlertException("A new textSubmission cannot already have an ID", ENTITY_NAME, "idexists");
        }

        return handleTextSubmission(exerciseId, principal, textSubmission);
    }

    /**
     * PUT /exercises/{exerciseId}/text-submissions : Updates an existing textSubmission. This function is called by the text editor for saving and submitting text submissions. The
     * submit specific handling occurs in the TextSubmissionService.save() function.
     *
     * @param exerciseId     the id of the exercise for which to init a participation
     * @param principal      the current user principal
     * @param textSubmission the textSubmission to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated textSubmission, or with status 400 (Bad Request) if the textSubmission is not valid, or with status
     *         500 (Internal Server Error) if the textSubmission couldn't be updated
     */
    @PutMapping("/exercises/{exerciseId}/text-submissions")
    @PreAuthorize("hasAnyRole('USER', 'TA', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<TextSubmission> updateTextSubmission(@PathVariable Long exerciseId, Principal principal, @RequestBody TextSubmission textSubmission) {
        log.debug("REST request to update TextSubmission : {}", textSubmission);
        if (textSubmission.getId() == null) {
            return createTextSubmission(exerciseId, principal, textSubmission);
        }
        return handleTextSubmission(exerciseId, principal, textSubmission);
    }

    @NotNull
    private ResponseEntity<TextSubmission> handleTextSubmission(Long exerciseId, Principal principal, TextSubmission textSubmission) {
        final User user = userService.getUserWithGroupsAndAuthorities();
        final TextExercise textExercise = textExerciseService.findOne(exerciseId);

        // fetch course from database to make sure client didn't change groups
        final Course course = courseService.findOne(textExercise.getCourseViaExerciseGroupOrCourseMember().getId());
        if (!authorizationCheckService.isAtLeastStudentInCourse(course, user)) {
            return forbidden();
        }

        // TODO: add one additional check: fetch textSubmission.getId() from the database with the corresponding participation and check that the user of participation is the
        // same as the user who executes this call. This prevents injecting submissions to other users

        textSubmission = textSubmissionService.handleTextSubmission(textSubmission, textExercise, principal);

        this.textSubmissionService.hideDetails(textSubmission, user);
        return ResponseEntity.ok(textSubmission);
    }

    /**
     * GET /text-submissions/:id : get the "id" textSubmission.
     *
     * @param id the id of the textSubmission to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the textSubmission, or with status 404 (Not Found)
     */
    @GetMapping("/text-submissions/{id}")
    public ResponseEntity<TextSubmission> getTextSubmission(@PathVariable Long id) {
        log.debug("REST request to get TextSubmission : {}", id);
        Optional<TextSubmission> textSubmission = textSubmissionRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(textSubmission);
    }

    /**
     * GET /text-submissions : get all the textSubmissions for an exercise. It is possible to filter, to receive only the one that have been already submitted, or only the one
     * assessed by the tutor who is doing the call
     *
     * @param exerciseId exerciseID  for which all submissions should be returned
     * @param submittedOnly mark if only submitted Submissions should be returned
     * @param assessedByTutor mark if only assessed Submissions should be returned
     * @return the ResponseEntity with status 200 (OK) and the list of textSubmissions in body
     */
    @GetMapping(value = "/exercises/{exerciseId}/text-submissions")
    @PreAuthorize("hasAnyRole('TA', 'INSTRUCTOR', 'ADMIN')")
    // TODO: separate this into 2 calls, one for instructors (with all submissions) and one for tutors (only the submissions for the requesting tutor)
    public ResponseEntity<List<TextSubmission>> getAllTextSubmissions(@PathVariable Long exerciseId, @RequestParam(defaultValue = "false") boolean submittedOnly,
            @RequestParam(defaultValue = "false") boolean assessedByTutor) {
        log.debug("REST request to get all TextSubmissions");
        User user = userService.getUserWithGroupsAndAuthorities();
        Exercise exercise = textExerciseService.findOne(exerciseId);
        if (assessedByTutor) {
            if (!authorizationCheckService.isAtLeastTeachingAssistantForExercise(exercise)) {
                throw new AccessForbiddenException("You are not allowed to access this resource");
            }
        }
        else if (!authorizationCheckService.isAtLeastInstructorForExercise(exercise)) {
            throw new AccessForbiddenException("You are not allowed to access this resource");
        }

        final List<TextSubmission> textSubmissions;
        if (assessedByTutor) {
            textSubmissions = textSubmissionService.getAllTextSubmissionsByTutorForExercise(exerciseId, user.getId());
        }
        else {
            textSubmissions = textSubmissionService.getTextSubmissionsByExerciseId(exerciseId, submittedOnly);
        }

        // tutors should not see information about the student of a submission
        if (!authorizationCheckService.isAtLeastInstructorForExercise(exercise, user)) {
            textSubmissions.forEach(submission -> textSubmissionService.hideDetails(submission, user));
        }

        // remove unnecessary data from the REST response
        textSubmissions.forEach(submission -> {
            if (submission.getParticipation() != null && submission.getParticipation().getExercise() != null) {
                submission.getParticipation().setExercise(null);
            }
        });

        return ResponseEntity.ok().body(textSubmissions);
    }

    /**
     * GET /text-submission-without-assessment : get one textSubmission without assessment.
     *
     * @param exerciseId exerciseID  for which a submission should be returned
     * TODO: Replace ?head=true with HTTP HEAD request
     * @param skipAssessmentOrderOptimization optional value to define if the assessment queue should be skipped. Use if only checking for needed assessments.
     * @param lockSubmission optional value to define if the submission should be locked and has the value of false if not set manually
     * @return the ResponseEntity with status 200 (OK) and the list of textSubmissions in body
     */
    @GetMapping(value = "/exercises/{exerciseId}/text-submission-without-assessment")
    @PreAuthorize("hasAnyRole('TA', 'INSTRUCTOR', 'ADMIN')")
    public ResponseEntity<TextSubmission> getTextSubmissionWithoutAssessment(@PathVariable Long exerciseId,
            @RequestParam(value = "head", defaultValue = "false") boolean skipAssessmentOrderOptimization,
            @RequestParam(value = "lock", defaultValue = "false") boolean lockSubmission) {
        log.debug("REST request to get a text submission without assessment");
        Exercise exercise = exerciseService.findOne(exerciseId);

        if (!authorizationCheckService.isAtLeastTeachingAssistantForExercise(exercise)) {
            return forbidden();
        }
        if (!(exercise instanceof TextExercise)) {
            return badRequest();
        }

        // Tutors cannot start assessing submissions if the exercise due date hasn't been reached yet
        if (exercise.getDueDate() != null && exercise.getDueDate().isAfter(ZonedDateTime.now())) {
            return notFound();
        }

        // Tutors cannot start assessing submissions if Athene is currently processing
        if (textClusteringScheduleService.isPresent() && textClusteringScheduleService.get().currentlyProcessing((TextExercise) exercise)) {
            return notFound();
        }

        // Check if the limit of simultaneously locked submissions has been reached
        textSubmissionService.checkSubmissionLockLimit(exercise.getCourseViaExerciseGroupOrCourseMember().getId());

        final TextSubmission textSubmission;
        if (lockSubmission) {
            textSubmission = textSubmissionService.findAndLockTextSubmissionToBeAssessed((TextExercise) exercise);
            textAssessmentService.prepareSubmissionForAssessment(textSubmission);
        }
        else {
            Optional<TextSubmission> optionalTextSubmission;
            if (skipAssessmentOrderOptimization) {
                optionalTextSubmission = textSubmissionService.getTextSubmissionWithoutManualResult((TextExercise) exercise, true);
            }
            else {
                optionalTextSubmission = this.textSubmissionService.getTextSubmissionWithoutManualResult((TextExercise) exercise);
            }
            if (optionalTextSubmission.isEmpty()) {
                return notFound();
            }
            textSubmission = optionalTextSubmission.get();
        }

        List<GradingCriterion> gradingCriteria = gradingCriterionService.findByExerciseIdWithEagerGradingCriteria(exerciseId);
        exercise.setGradingCriteria(gradingCriteria);

        // Make sure the exercise is connected to the participation in the json response
        final StudentParticipation studentParticipation = (StudentParticipation) textSubmission.getParticipation();
        studentParticipation.setExercise(exercise);
        textSubmission.getParticipation().getExercise().setGradingCriteria(gradingCriteria);
        textSubmissionService.hideDetails(textSubmission, userService.getUserWithGroupsAndAuthorities());
        return ResponseEntity.ok(textSubmission);
    }
}
