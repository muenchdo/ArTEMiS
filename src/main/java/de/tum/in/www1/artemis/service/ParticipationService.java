package de.tum.in.www1.artemis.service;

import static de.tum.in.www1.artemis.domain.enumeration.InitializationState.*;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.tum.in.www1.artemis.domain.*;
import de.tum.in.www1.artemis.domain.enumeration.AssessmentType;
import de.tum.in.www1.artemis.domain.enumeration.BuildPlanType;
import de.tum.in.www1.artemis.domain.enumeration.InitializationState;
import de.tum.in.www1.artemis.domain.enumeration.SubmissionType;
import de.tum.in.www1.artemis.domain.modeling.ModelingExercise;
import de.tum.in.www1.artemis.domain.modeling.ModelingSubmission;
import de.tum.in.www1.artemis.domain.participation.*;
import de.tum.in.www1.artemis.domain.quiz.QuizExercise;
import de.tum.in.www1.artemis.domain.quiz.QuizSubmission;
import de.tum.in.www1.artemis.exception.VersionControlException;
import de.tum.in.www1.artemis.repository.*;
import de.tum.in.www1.artemis.service.connectors.ContinuousIntegrationService;
import de.tum.in.www1.artemis.service.connectors.GitService;
import de.tum.in.www1.artemis.service.connectors.VersionControlService;
import de.tum.in.www1.artemis.service.scheduled.QuizScheduleService;
import de.tum.in.www1.artemis.web.rest.errors.EntityNotFoundException;

/**
 * Service Implementation for managing Participation.
 */
@Service
public class ParticipationService {

    private final Logger log = LoggerFactory.getLogger(ParticipationService.class);

    private final ParticipationRepository participationRepository;

    private final StudentParticipationRepository studentParticipationRepository;

    private final ProgrammingExerciseStudentParticipationRepository programmingExerciseStudentParticipationRepository;

    private final TemplateProgrammingExerciseParticipationRepository templateProgrammingExerciseParticipationRepository;

    private final SolutionProgrammingExerciseParticipationRepository solutionProgrammingExerciseParticipationRepository;

    private final ExerciseRepository exerciseRepository;

    private final ResultRepository resultRepository;

    private final SubmissionRepository submissionRepository;

    private final ComplaintResponseRepository complaintResponseRepository;

    private final ComplaintRepository complaintRepository;

    private final TeamRepository teamRepository;

    private final UserService userService;

    private final GitService gitService;

    private final Optional<ContinuousIntegrationService> continuousIntegrationService;

    private final Optional<VersionControlService> versionControlService;

    private final ConflictingResultService conflictingResultService;

    private final AuthorizationCheckService authCheckService;

    public ParticipationService(ProgrammingExerciseStudentParticipationRepository programmingExerciseStudentParticipationRepository,
            TemplateProgrammingExerciseParticipationRepository templateProgrammingExerciseParticipationRepository,
            SolutionProgrammingExerciseParticipationRepository solutionProgrammingExerciseParticipationRepository, ParticipationRepository participationRepository,
            StudentParticipationRepository studentParticipationRepository, ExerciseRepository exerciseRepository, ResultRepository resultRepository,
            SubmissionRepository submissionRepository, ComplaintResponseRepository complaintResponseRepository, ComplaintRepository complaintRepository,
            TeamRepository teamRepository, UserService userService, GitService gitService, Optional<ContinuousIntegrationService> continuousIntegrationService,
            Optional<VersionControlService> versionControlService, ConflictingResultService conflictingResultService, AuthorizationCheckService authCheckService) {
        this.participationRepository = participationRepository;
        this.programmingExerciseStudentParticipationRepository = programmingExerciseStudentParticipationRepository;
        this.templateProgrammingExerciseParticipationRepository = templateProgrammingExerciseParticipationRepository;
        this.solutionProgrammingExerciseParticipationRepository = solutionProgrammingExerciseParticipationRepository;
        this.studentParticipationRepository = studentParticipationRepository;
        this.exerciseRepository = exerciseRepository;
        this.resultRepository = resultRepository;
        this.submissionRepository = submissionRepository;
        this.complaintResponseRepository = complaintResponseRepository;
        this.complaintRepository = complaintRepository;
        this.teamRepository = teamRepository;
        this.userService = userService;
        this.gitService = gitService;
        this.continuousIntegrationService = continuousIntegrationService;
        this.versionControlService = versionControlService;
        this.conflictingResultService = conflictingResultService;
        this.authCheckService = authCheckService;
    }

    /**
     * Save any type of participation without knowing the explicit subtype
     *
     * @param participation the entity to save
     * @return the persisted entity
     */
    public Participation save(ParticipationInterface participation) {
        // Note: unfortunately polymorphism does not always work in Hibernate due to reflective method invocation.
        // Therefore we provide a convenience method here that finds out the concrete participation type and delegates the call to the right method
        if (participation instanceof ProgrammingExerciseStudentParticipation) {
            return save((ProgrammingExerciseStudentParticipation) participation);
        }
        else if (participation instanceof StudentParticipation) {
            return save((StudentParticipation) participation);
        }
        else if (participation instanceof TemplateProgrammingExerciseParticipation) {
            return save((TemplateProgrammingExerciseParticipation) participation);
        }
        else if (participation instanceof SolutionProgrammingExerciseParticipation) {
            return save((SolutionProgrammingExerciseParticipation) participation);
        }
        else {
            throw new RuntimeException("Participation type not known. Cannot save!");
        }
    }

    /**
     * Save a ProgrammingExerciseStudentParticipation.
     *
     * @param participation the entity to save
     * @return the persisted entity
     */
    public ProgrammingExerciseStudentParticipation save(ProgrammingExerciseStudentParticipation participation) {
        log.debug("Request to save ProgrammingExerciseStudentParticipation : {}", participation);
        return studentParticipationRepository.saveAndFlush(participation);
    }

    /**
     * Save a StudentParticipation.
     *
     * @param participation the entity to save
     * @return the persisted entity
     */
    public StudentParticipation save(StudentParticipation participation) {
        log.debug("Request to save StudentParticipation : {}", participation);
        return studentParticipationRepository.saveAndFlush(participation);
    }

    /**
     * Save a TemplateProgrammingExerciseParticipation.
     *
     * @param participation the entity to save
     * @return the persisted entity
     */
    public TemplateProgrammingExerciseParticipation save(TemplateProgrammingExerciseParticipation participation) {
        log.debug("Request to save TemplateProgrammingExerciseParticipation : {}", participation);
        return templateProgrammingExerciseParticipationRepository.saveAndFlush(participation);
    }

    /**
     * Save a SolutionProgrammingExerciseParticipation.
     *
     * @param participation the entity to save
     * @return the persisted entity
     */
    public SolutionProgrammingExerciseParticipation save(SolutionProgrammingExerciseParticipation participation) {
        log.debug("Request to save SolutionProgrammingExerciseParticipation : {}", participation);
        return solutionProgrammingExerciseParticipationRepository.saveAndFlush(participation);
    }

    /**
     * This method is triggered when a student starts an exercise. It creates a Participation which connects the corresponding student and exercise. Additionally, it configures
     * repository / build plan related stuff for programming exercises. In the case of modeling or text exercises, it also initializes and stores the corresponding submission.
     *
     * @param exercise the exercise which is started
     * @param participant the user or team who starts the exercise
     * @return the participation connecting the given exercise and user
     */
    public StudentParticipation startExercise(Exercise exercise, Participant participant) {
        // common for all exercises
        // Check if participation already exists
        Optional<StudentParticipation> optionalStudentParticipation = findOneByExerciseAndParticipantAnyState(exercise, participant);
        StudentParticipation participation;
        if (optionalStudentParticipation.isEmpty()) {
            // create a new participation only if no participation can be found
            if (exercise instanceof ProgrammingExercise) {
                participation = new ProgrammingExerciseStudentParticipation();
            }
            else {
                participation = new StudentParticipation();
            }
            participation.setInitializationState(UNINITIALIZED);
            participation.setExercise(exercise);
            participation.setParticipant(participant);

            participation = save(participation);
        }
        else {
            // make sure participation and exercise are connected
            participation = optionalStudentParticipation.get();
            participation.setExercise(exercise);
        }

        // specific to programming exercises
        if (exercise instanceof ProgrammingExercise) {
            ProgrammingExerciseStudentParticipation programmingExerciseStudentParticipation = (ProgrammingExerciseStudentParticipation) participation;
            programmingExerciseStudentParticipation = copyRepository(programmingExerciseStudentParticipation);
            programmingExerciseStudentParticipation = configureRepository((ProgrammingExercise) exercise, programmingExerciseStudentParticipation);
            programmingExerciseStudentParticipation = copyBuildPlan(programmingExerciseStudentParticipation);
            programmingExerciseStudentParticipation = configureBuildPlan(programmingExerciseStudentParticipation);
            // we might need to perform an empty commit (depends on the CI system), we perform this here, because it should not trigger a new programming submission
            programmingExerciseStudentParticipation = performEmptyCommit(programmingExerciseStudentParticipation);
            // Note: we configure the repository webhook last, so that the potential empty commit does not trigger a new programming submission (see empty-commit-necessary)
            programmingExerciseStudentParticipation = configureRepositoryWebHook(programmingExerciseStudentParticipation);
            programmingExerciseStudentParticipation.setInitializationState(INITIALIZED);
            programmingExerciseStudentParticipation.setInitializationDate(ZonedDateTime.now());
            // after saving, we need to make sure the object that is used after the if statement is the right one
            participation = programmingExerciseStudentParticipation;
        }
        else {// for all other exercises: QuizExercise, ModelingExercise, TextExercise, FileUploadExercise
            if (participation.getInitializationState() == null || participation.getInitializationState() == UNINITIALIZED || participation.getInitializationState() == FINISHED) {
                // in case the participation was finished before, we set it to initialized again so that the user sees the correct button "Open modeling editor" on the client side
                participation.setInitializationState(INITIALIZED);
            }

            if (Optional.ofNullable(participation.getInitializationDate()).isEmpty()) {
                participation.setInitializationDate(ZonedDateTime.now());
            }

            if (optionalStudentParticipation.isEmpty() || !submissionRepository.existsByParticipationId(participation.getId())) {
                // initialize a modeling, text or file upload submission (depending on the exercise type), it will not do anything in the case of a quiz exercise
                initializeSubmission(participation, exercise, null);
            }
        }
        participation = save(participation);

        return participation;
    }

    /**
     * This method checks whether a participation exists for a given exercise and user. If not, it creates such a participation with initialization state FINISHED.
     * If the participation had to be newly created or there were no submissions yet for the existing participation, a new submission is created with the given submission type.
     * For external submissions, the submission is assumed to be submitted immediately upon creation.
     *
     * @param exercise the exercise for which to create a participation and submission
     * @param participant the user/team for which to create a participation and submission
     * @param submissionType the type of submission to create if none exist yet
     * @return the participation connecting the given exercise and user
     */
    public StudentParticipation createParticipationWithEmptySubmissionIfNotExisting(Exercise exercise, Participant participant, SubmissionType submissionType) {
        Optional<StudentParticipation> optionalStudentParticipation = findOneByExerciseAndParticipantAnyState(exercise, participant);
        StudentParticipation participation;
        if (optionalStudentParticipation.isEmpty()) {
            // create a new participation only if no participation can be found
            if (exercise instanceof ProgrammingExercise) {
                participation = new ProgrammingExerciseStudentParticipation();
            }
            else {
                participation = new StudentParticipation();
            }
            participation.setInitializationState(UNINITIALIZED);
            participation.setInitializationDate(ZonedDateTime.now());
            participation.setExercise(exercise);
            participation.setParticipant(participant);

            participation = save(participation);
        }
        else {
            participation = optionalStudentParticipation.get();
        }

        // setup repository in case of programming exercise
        if (exercise instanceof ProgrammingExercise) {
            ProgrammingExercise programmingExercise = (ProgrammingExercise) exercise;
            ProgrammingExerciseStudentParticipation programmingParticipation = (ProgrammingExerciseStudentParticipation) participation;
            // Note: we need a repository, otherwise the student cannot click resume.
            programmingParticipation = copyRepository(programmingParticipation);
            programmingParticipation = configureRepository(programmingExercise, programmingParticipation);
            programmingParticipation = configureRepositoryWebHook(programmingParticipation);
            participation = programmingParticipation;
            if (programmingExercise.getBuildAndTestStudentSubmissionsAfterDueDate() != null || programmingExercise.getAssessmentType() != AssessmentType.AUTOMATIC) {
                // restrict access for the student
                try {
                    versionControlService.get().setRepositoryPermissionsToReadOnly(programmingParticipation.getRepositoryUrlAsUrl(), programmingExercise.getProjectKey(),
                            programmingParticipation.getStudents());
                }
                catch (VersionControlException e) {
                    log.error("Removing write permissions failed for programming exercise with id " + programmingExercise.getId() + " for student repository with participation id "
                            + programmingParticipation.getId() + ": " + e.getMessage());
                }
            }
        }

        participation.setInitializationState(FINISHED);
        participation = save(participation);

        // Take the latest submission or initialize a new empty submission
        participation = (StudentParticipation) findOneWithEagerSubmissions(participation.getId());
        Optional<Submission> optionalSubmission = participation.findLatestSubmission();
        Submission submission;
        if (optionalSubmission.isPresent()) {
            submission = optionalSubmission.get();
        }
        else {
            submission = initializeSubmission(participation, exercise, submissionType).get();
            participation = save(participation);
        }

        // If the submission has not yet been submitted, submit it now
        if (!submission.isSubmitted()) {
            submission.setSubmitted(true);
            submission.setSubmissionDate(ZonedDateTime.now());
            submissionRepository.save(submission);
        }

        return participation;
    }

    /**
     * Initializes a new text, modeling or file upload submission (depending on the type of the given exercise), connects it with the given participation and stores it in the
     * database.
     *
     * @param participation                 the participation for which the submission should be initialized
     * @param exercise                      the corresponding exercise, should be either a text, modeling or file upload exercise, otherwise it will instantly return and not do anything
     * @param submissionType                type for the submission to be initialized
     */
    private Optional<Submission> initializeSubmission(Participation participation, Exercise exercise, SubmissionType submissionType) {
        if (exercise instanceof QuizExercise) {
            return Optional.empty();
        }

        Submission submission;
        if (exercise instanceof ProgrammingExercise) {
            submission = new ProgrammingSubmission();
        }
        else if (exercise instanceof ModelingExercise) {
            submission = new ModelingSubmission();
        }
        else if (exercise instanceof TextExercise) {
            submission = new TextSubmission();
        }
        else {
            submission = new FileUploadSubmission();
        }

        submission.setType(submissionType);
        submission.setParticipation(participation);
        submissionRepository.save(submission);
        participation.addSubmissions(submission);
        return Optional.of(submission);
    }

    /**
     * Get a participation for the given quiz and username.
     * If the quiz hasn't ended, participation is constructed from cached submission.
     * If the quiz has ended, we first look in the database for the participation and construct one if none was found
     *
     * @param quizExercise the quiz exercise to attach to the participation
     * @param username     the username of the user that the participation belongs to
     * @return the found or created participation with a result
     */
    public StudentParticipation participationForQuizWithResult(QuizExercise quizExercise, String username) {
        if (quizExercise.isEnded()) {
            // try getting participation from database
            Optional<StudentParticipation> optionalParticipation = findOneByExerciseAndStudentLoginAnyState(quizExercise, username);

            if (optionalParticipation.isEmpty()) {
                log.error("Participation in quiz " + quizExercise.getTitle() + " not found for user " + username);
                // TODO properly handle this case
                return null;
            }
            StudentParticipation participation = optionalParticipation.get();
            // add exercise
            participation.setExercise(quizExercise);

            // add the appropriate result
            Result result = resultRepository.findFirstByParticipationIdAndRatedOrderByCompletionDateDesc(participation.getId(), true).orElse(null);

            participation.setResults(new HashSet<>());

            if (result != null) {
                participation.addResult(result);
            }

            return participation;
        }

        // Look for Participation in ParticipationHashMap first
        StudentParticipation participation = QuizScheduleService.getParticipation(quizExercise.getId(), username);
        if (participation != null) {
            return participation;
        }

        // get submission from HashMap
        QuizSubmission quizSubmission = QuizScheduleService.getQuizSubmission(quizExercise.getId(), username);

        // TODO: SK I guess we can delete this code, because it is unreachable as we return above in case the quiz has ended
        if (quizExercise.isEnded() && quizSubmission.getSubmissionDate() != null) {
            if (quizSubmission.isSubmitted()) {
                quizSubmission.setType(SubmissionType.MANUAL);
            }
            else {
                quizSubmission.setSubmitted(true);
                quizSubmission.setType(SubmissionType.TIMEOUT);
                quizSubmission.setSubmissionDate(ZonedDateTime.now());
            }
        }

        // construct result
        Result result = new Result().submission(quizSubmission);

        // construct participation
        participation = new StudentParticipation();
        participation.setInitializationState(INITIALIZED);
        participation.setExercise(quizExercise);
        participation.addResult(result);

        // TODO: SK I guess we can delete this code, because it is unreachable as we return above in case the quiz has ended
        if (quizExercise.isEnded() && quizSubmission.getSubmissionDate() != null) {
            // update result and participation state
            result.setRated(true);
            result.setAssessmentType(AssessmentType.AUTOMATIC);
            result.setCompletionDate(ZonedDateTime.now());
            participation.setInitializationState(InitializationState.FINISHED);

            // calculate scores and update result and submission accordingly
            quizSubmission.calculateAndUpdateScores(quizExercise);
            result.evaluateSubmission();
        }

        return participation;
    }

    /*
     * Get all submissions of a participationId
     * @param participationId of submission
     * @return List<submission>
     */
    public List<Submission> getSubmissionsWithParticipationId(long participationId) {
        return submissionRepository.findAllByParticipationId(participationId);
    }

    /**
     * Service method to resume inactive participation (with previously deleted build plan)
     *
     * @param participation inactive participation
     * @return resumed participation
     */
    public ProgrammingExerciseStudentParticipation resumeExercise(ProgrammingExerciseStudentParticipation participation) {
        participation = copyBuildPlan(participation);
        participation = configureBuildPlan(participation);
        // Note: the repository webhook already exists so we don't need to set it up again
        participation.setInitializationState(INITIALIZED);
        participation = save(participation);
        if (participation.getInitializationDate() == null) {
            // only set the date if it was not set before (which should NOT be the case)
            participation.setInitializationDate(ZonedDateTime.now());
        }
        return save(participation);
    }

    private ProgrammingExerciseStudentParticipation copyRepository(ProgrammingExerciseStudentParticipation participation) {
        // only execute this step if it has not yet been completed yet or if the repository url is missing for some reason
        if (!participation.getInitializationState().hasCompletedState(InitializationState.REPO_COPIED) || participation.getRepositoryUrlAsUrl() == null) {
            final var programmingExercise = participation.getProgrammingExercise();
            final var projectKey = programmingExercise.getProjectKey();
            final var participantIdentifier = participation.getParticipantIdentifier();
            // NOTE: we have to get the repository slug of the template participation here, because not all exercises (in particular old ones) follow the naming conventions
            final var repoName = versionControlService.get().getRepositorySlugFromUrl(programmingExercise.getTemplateParticipation().getRepositoryUrlAsUrl());
            // the next action includes recovery, which means if the repository has already been copied, we simply retrieve the repository url and do not copy it again
            var newRepoUrl = versionControlService.get().copyRepository(projectKey, repoName, projectKey, participantIdentifier);
            // add the userInfo part to the repoURL only if the participation belongs to a single student (and not a team of students)
            if (participation.getStudent().isPresent()) {
                newRepoUrl = newRepoUrl.withUser(participantIdentifier);
            }
            participation.setRepositoryUrl(newRepoUrl.toString());
            participation.setInitializationState(REPO_COPIED);

            return save(participation);
        }
        else {
            return participation;
        }
    }

    private ProgrammingExerciseStudentParticipation configureRepository(ProgrammingExercise exercise, ProgrammingExerciseStudentParticipation participation) {
        if (!participation.getInitializationState().hasCompletedState(InitializationState.REPO_CONFIGURED)) {
            versionControlService.get().configureRepository(exercise, participation.getRepositoryUrlAsUrl(), participation.getStudents());
            participation.setInitializationState(InitializationState.REPO_CONFIGURED);
            return save(participation);
        }
        else {
            return participation;
        }
    }

    private ProgrammingExerciseStudentParticipation copyBuildPlan(ProgrammingExerciseStudentParticipation participation) {
        // only execute this step if it has not yet been completed yet or if the build plan id is missing for some reason
        if (!participation.getInitializationState().hasCompletedState(InitializationState.BUILD_PLAN_COPIED) || participation.getBuildPlanId() == null) {
            final var projectKey = participation.getProgrammingExercise().getProjectKey();
            final var planName = BuildPlanType.TEMPLATE.getName();
            final var username = participation.getParticipantIdentifier();
            final var buildProjectName = participation.getExercise().getCourseViaExerciseGroupOrCourseMember().getShortName().toUpperCase() + " "
                    + participation.getExercise().getTitle();
            // the next action includes recovery, which means if the build plan has already been copied, we simply retrieve the build plan id and do not copy it again
            final var buildPlanId = continuousIntegrationService.get().copyBuildPlan(projectKey, planName, projectKey, buildProjectName, username.toUpperCase());
            participation.setBuildPlanId(buildPlanId);
            participation.setInitializationState(InitializationState.BUILD_PLAN_COPIED);
            return save(participation);
        }
        else {
            return participation;
        }
    }

    private ProgrammingExerciseStudentParticipation configureBuildPlan(ProgrammingExerciseStudentParticipation participation) {
        if (!participation.getInitializationState().hasCompletedState(InitializationState.BUILD_PLAN_CONFIGURED)) {
            continuousIntegrationService.get().configureBuildPlan(participation);
            participation.setInitializationState(InitializationState.BUILD_PLAN_CONFIGURED);
            return save(participation);
        }
        else {
            return participation;
        }
    }

    private ProgrammingExerciseStudentParticipation configureRepositoryWebHook(ProgrammingExerciseStudentParticipation participation) {
        if (!participation.getInitializationState().hasCompletedState(InitializationState.INITIALIZED)) {
            versionControlService.get().addWebHookForParticipation(participation);
        }
        return participation;
    }

    /**
     * Perform an empty commit so that the build plan definitely runs for the actual student commit
     *
     * @param participation the participation of the student
     * @return the participation of the student
     */
    public ProgrammingExerciseStudentParticipation performEmptyCommit(ProgrammingExerciseStudentParticipation participation) {
        continuousIntegrationService.get().performEmptySetupCommit(participation);
        return participation;
    }

    /**
     * Get one participation by id.
     *
     * @param participationId the id of the entity
     * @return the entity
     * @throws EntityNotFoundException throws if participation was not found
     **/
    public Participation findOne(Long participationId) throws EntityNotFoundException {
        log.debug("Request to get Participation : {}", participationId);
        Optional<Participation> participation = participationRepository.findById(participationId);
        if (participation.isEmpty()) {
            throw new EntityNotFoundException("Participation with " + participationId + " was not found!");
        }
        return participation.get();
    }

    /**
     * Get one student participation by id.
     *
     * @param participationId the id of the entity
     * @return the entity
     **/
    public StudentParticipation findOneStudentParticipation(Long participationId) {
        log.debug("Request to get Participation : {}", participationId);
        Optional<StudentParticipation> participation = studentParticipationRepository.findById(participationId);
        if (participation.isEmpty()) {
            throw new EntityNotFoundException("StudentParticipation with " + participationId + " was not found!");
        }
        return participation.get();
    }

    /**
     * Get one student participation by id with fetched Result, Submissions, Exercise and Course.
     *
     * @param participationId the id of the entity
     * @return the entity
     **/
    public StudentParticipation findOneStudentParticipationWithEagerSubmissionsResultsExerciseAndCourse(Long participationId) {
        log.debug("Request to get Participation : {}", participationId);
        Optional<StudentParticipation> participation = studentParticipationRepository.findWithEagerSubmissionsAndResultsAndExerciseAndCourseById(participationId);
        if (participation.isEmpty()) {
            throw new EntityNotFoundException("StudentParticipation with " + participationId + " was not found!");
        }
        return participation.get();
    }

    /**
     * Get one programming exercise participation by id.
     *
     * @param participationId the id of the entity
     * @return the entity
     */
    public ProgrammingExerciseStudentParticipation findProgrammingExerciseParticipation(Long participationId) {
        log.debug("Request to get Participation : {}", participationId);
        Optional<ProgrammingExerciseStudentParticipation> participation = programmingExerciseStudentParticipationRepository.findById(participationId);
        if (participation.isEmpty()) {
            throw new EntityNotFoundException("ProgrammingExerciseStudentParticipation with " + participationId + " was not found!");
        }
        return participation.get();
    }

    /**
     * Get one participation by id including all results.
     *
     * @param participationId the id of the participation
     * @return the participation with all its results
     */
    public StudentParticipation findOneWithEagerResults(Long participationId) {
        log.debug("Request to get Participation : {}", participationId);
        Optional<StudentParticipation> participation = studentParticipationRepository.findByIdWithEagerResults(participationId);
        if (participation.isEmpty()) {
            throw new EntityNotFoundException("Participation with " + participationId + " was not found!");
        }
        return participation.get();
    }

    /**
     * Get one participation by id including all submissions. Throws an EntityNotFoundException if the participation with the given id could not be found.
     *
     * @param participationId the id of the entity
     * @return the participation with all its submissions and results
     */
    public Participation findOneWithEagerSubmissions(Long participationId) {
        log.debug("Request to get Participation : {}", participationId);
        Optional<Participation> participation = participationRepository.findWithEagerSubmissionsById(participationId);
        if (participation.isEmpty()) {
            throw new EntityNotFoundException("Participation with " + participationId + " was not found!");
        }
        return participation.get();
    }

    /**
     * Get one participation by id including all submissions and results. Throws an EntityNotFoundException if the participation with the given id could not be found.
     *
     * @param participationId the id of the entity
     * @return the participation with all its submissions and results
     */
    public StudentParticipation findOneWithEagerSubmissionsAndResults(Long participationId) {
        log.debug("Request to get Participation : {}", participationId);
        Optional<StudentParticipation> participation = studentParticipationRepository.findWithEagerSubmissionsAndResultsById(participationId);
        if (participation.isEmpty()) {
            throw new EntityNotFoundException("Participation with " + participationId + " was not found!");
        }
        return participation.get();
    }

    /**
     * Get one participation (in any state) by its student and exercise.
     *
     * @param exercise the exercise for which to find a participation
     * @param username the username of the student
     * @return the participation of the given student and exercise in any state
     */
    public Optional<StudentParticipation> findOneByExerciseAndStudentLoginAnyState(Exercise exercise, String username) {
        log.debug("Request to get Participation for User {} for Exercise with id: {}", username, exercise.getId());
        if (exercise.isTeamMode()) {
            Optional<Team> optionalTeam = teamRepository.findOneByExerciseIdAndUserLogin(exercise.getId(), username);
            return optionalTeam.flatMap(team -> studentParticipationRepository.findByExerciseIdAndTeamId(exercise.getId(), team.getId()));
        }
        return studentParticipationRepository.findByExerciseIdAndStudentLogin(exercise.getId(), username);
    }

    /**
     * Get one participation (in any state) by its team and exercise.
     *
     * @param exercise the exercise for which to find a participation
     * @param teamId the id of the team
     * @return the participation of the given team and exercise in any state
     */
    public Optional<StudentParticipation> findOneByExerciseAndTeamIdAnyState(Exercise exercise, Long teamId) {
        log.debug("Request to get Participation for Team with id {} for Exercise with id: {}", teamId, exercise.getId());
        return studentParticipationRepository.findByExerciseIdAndTeamId(exercise.getId(), teamId);
    }

    /**
     * Get one participation (in any state) by its participant and exercise.
     *
     * @param exercise the exercise for which to find a participation
     * @param participant the short name of the team
     * @return the participation of the given team and exercise in any state
     */
    public Optional<StudentParticipation> findOneByExerciseAndParticipantAnyState(Exercise exercise, Participant participant) {
        if (participant instanceof User) {
            return findOneByExerciseAndStudentLoginAnyState(exercise, ((User) participant).getLogin());
        }
        else if (participant instanceof Team) {
            return findOneByExerciseAndTeamIdAnyState(exercise, ((Team) participant).getId());
        }
        else {
            throw new Error("Unknown Participant type");
        }
    }

    /**
     * Get one participation (in any state) by its student and exercise with all its results.
     *
     * @param exercise the exercise for which to find a participation
     * @param username   the username of the student
     * @return the participation of the given student and exercise in any state
     */
    public Optional<StudentParticipation> findOneByExerciseAndStudentLoginAnyStateWithEagerResults(Exercise exercise, String username) {
        log.debug("Request to get Participation for User {} for Exercise with id: {}", username, exercise.getId());
        if (exercise.isTeamMode()) {
            Optional<Team> optionalTeam = teamRepository.findOneByExerciseIdAndUserLogin(exercise.getId(), username);
            return optionalTeam.flatMap(team -> studentParticipationRepository.findWithEagerResultsByExerciseIdAndTeamId(exercise.getId(), team.getId()));
        }
        return studentParticipationRepository.findWithEagerResultsByExerciseIdAndStudentLogin(exercise.getId(), username);
    }

    /**
     * Get one participation (in any state) by its student and exercise with eager submissions.
     *
     * @param exercise the exercise for which to find a participation
     * @param username   the username of the student
     * @return the participation of the given student and exercise with eager submissions in any state
     */
    public Optional<StudentParticipation> findOneByExerciseAndStudentLoginWithEagerSubmissionsAnyState(Exercise exercise, String username) {
        log.debug("Request to get Participation for User {} for Exercise with id: {}", username, exercise.getId());
        if (exercise.isTeamMode()) {
            Optional<Team> optionalTeam = teamRepository.findOneByExerciseIdAndUserLogin(exercise.getId(), username);
            return optionalTeam.flatMap(team -> studentParticipationRepository.findWithEagerSubmissionsByExerciseIdAndTeamId(exercise.getId(), team.getId()));
        }
        return studentParticipationRepository.findWithEagerSubmissionsByExerciseIdAndStudentLogin(exercise.getId(), username);
    }

    /**
     * Get all programming exercise participations belonging to build plan and initialize there state with eager results.
     *
     * @param buildPlanId the id of build plan
     * @return the list of programming exercise participations belonging to build plan
     */
    public List<ProgrammingExerciseStudentParticipation> findByBuildPlanIdWithEagerResults(String buildPlanId) {
        log.debug("Request to get Participation for build plan id: {}", buildPlanId);
        return programmingExerciseStudentParticipationRepository.findByBuildPlanId(buildPlanId);
    }

    /**
     * Get all programming exercise participations belonging to exercise.
     *
     * @param exerciseId the id of exercise
     * @return the list of programming exercise participations belonging to exercise
     */
    public List<StudentParticipation> findByExerciseId(Long exerciseId) {
        return studentParticipationRepository.findByExerciseId(exerciseId);
    }

    /**
     * Get all programming exercise participations belonging to team.
     *
     * @param teamId the id of team
     * @return the list of programming exercise participations belonging to team
     */
    public List<StudentParticipation> findByTeamId(Long teamId) {
        return studentParticipationRepository.findByTeamId(teamId);
    }

    /**
     * Get all programming exercise participations belonging to exercise with eager results.
     *
     * @param exerciseId the id of exercise
     * @return the list of programming exercise participations belonging to exercise
     */
    public List<StudentParticipation> findByExerciseIdWithLatestResult(Long exerciseId) {
        return studentParticipationRepository.findByExerciseIdWithLatestResult(exerciseId);
    }

    /**
     * Get all programming exercise participations belonging to exercise with eager submissions -> result.
     *
     * @param exerciseId the id of exercise
     * @return the list of programming exercise participations belonging to exercise
     */
    public List<StudentParticipation> findByExerciseIdWithEagerSubmissionsResult(Long exerciseId) {
        return studentParticipationRepository.findByExerciseIdWithEagerSubmissionsResult(exerciseId);
    }

    /**
     * Get all programming exercise participations belonging to exercise with eager submissions -> result --> assessor.
     *
     * @param exerciseId the id of exercise
     * @return the list of programming exercise participations belonging to exercise
     */
    public List<StudentParticipation> findByExerciseIdWithEagerSubmissionsResultAssessor(Long exerciseId) {
        return studentParticipationRepository.findByExerciseIdWithEagerSubmissionsResultAssessor(exerciseId);
    }

    /**
     * Get all programming exercise participations belonging to exercise and student with eager results and submissions.
     *
     * @param exercise  the exercise
     * @param studentId the id of student
     * @return the list of programming exercise participations belonging to exercise and student
     */
    public List<StudentParticipation> findByExerciseAndStudentIdWithEagerResultsAndSubmissions(Exercise exercise, Long studentId) {
        if (exercise.isTeamMode()) {
            Optional<Team> optionalTeam = teamRepository.findOneByExerciseIdAndUserId(exercise.getId(), studentId);
            return optionalTeam.map(team -> studentParticipationRepository.findByExerciseIdAndTeamIdWithEagerResultsAndSubmissions(exercise.getId(), team.getId()))
                    .orElse(List.of());
        }
        return studentParticipationRepository.findByExerciseIdAndStudentIdWithEagerResultsAndSubmissions(exercise.getId(), studentId);
    }

    /**
     * Get all exercise participations belonging to exercise and team with eager results and submissions.
     *
     * @param exercise the exercise
     * @param team     the team
     * @return the list of exercise participations belonging to exercise and team
     */
    public List<StudentParticipation> findByExerciseAndTeamWithEagerResultsAndSubmissions(Exercise exercise, Team team) {
        return studentParticipationRepository.findByExerciseIdAndTeamIdWithEagerResultsAndSubmissions(exercise.getId(), team.getId());
    }

    /**
     * Get all participations of submissions that are submitted and do not already have a manual result. No manual result means that no user has started an assessment for the
     * corresponding submission yet.
     *
     * @param exerciseId the id of the exercise the participations should belong to
     * @return a list of participations including their submitted submissions that do not have a manual result
     */
    public List<StudentParticipation> findByExerciseIdWithLatestSubmissionWithoutManualResults(Long exerciseId) {
        return studentParticipationRepository.findByExerciseIdWithLatestSubmissionWithoutManualResults(exerciseId);
    }

    /**
     * Get all participations belonging to course with relevant results.
     *
     * @param courseId the id of the exercise
     * @return list of participations belonging to course
     */
    public List<StudentParticipation> findByCourseIdWithRelevantResult(Long courseId) {
        List<StudentParticipation> participations = studentParticipationRepository.findByCourseIdWithEagerRatedResults(courseId);

        return participations.stream()

                // Filter out participations without Students
                // These participations are used e.g. to store template and solution build plans in programming exercises
                .filter(participation -> participation.getParticipant() != null)

                // filter all irrelevant results, i.e. rated = false or no completion date or no score
                .peek(participation -> {
                    List<Result> relevantResults = new ArrayList<Result>();

                    // search for the relevant result by filtering out irrelevant results using the continue keyword
                    // this for loop is optimized for performance and thus not very easy to understand ;)
                    for (Result result : participation.getResults()) {
                        // this should not happen because the database call above only retrieves rated results
                        if (Boolean.FALSE.equals(result.isRated())) {
                            continue;
                        }
                        if (result.getCompletionDate() == null || result.getScore() == null) {
                            // we are only interested in results with completion date and with score
                            continue;
                        }
                        relevantResults.add(result);
                    }
                    // we take the last rated result
                    if (!relevantResults.isEmpty()) {
                        // make sure to take the latest result
                        relevantResults.sort((r1, r2) -> r2.getCompletionDate().compareTo(r1.getCompletionDate()));
                        Result correctResult = relevantResults.get(0);
                        relevantResults.clear();
                        relevantResults.add(correctResult);
                    }
                    participation.setResults(new HashSet<>(relevantResults));
                }).collect(Collectors.toList());
    }

    /**
     * Get all exercise participations of a team in a course
     *
     * @param courseId the id of the course
     * @param teamShortName the team short name (all teams with this short name in the course are seen as the same team)
     * @return the list of exercise participations for the team in the course
     */
    public List<StudentParticipation> findAllByCourseIdAndTeamShortName(Long courseId, String teamShortName) {
        return studentParticipationRepository.findAllByCourseIdAndTeamShortName(courseId, teamShortName);
    }

    /**
     * Deletes the build plan on the continuous integration server and sets the initialization state of the participation to inactive This means the participation can be resumed in
     * the future
     *
     * @param participation that will be set to inactive
     */
    public void cleanupBuildPlan(ProgrammingExerciseStudentParticipation participation) {
        // ignore participations without build plan id
        if (participation.getBuildPlanId() != null) {
            final var projectKey = ((ProgrammingExercise) participation.getExercise()).getProjectKey();
            continuousIntegrationService.get().deleteBuildPlan(projectKey, participation.getBuildPlanId());
            participation.setInitializationState(INACTIVE);
            participation.setBuildPlanId(null);
            save(participation);
        }
    }

    /**
     * NOTICE: be careful with this method because it deletes the students code on the version control server Deletes the repository on the version control server and sets the
     * initialization state of the participation to finished This means the participation cannot be resumed in the future and would need to be restarted
     *
     * @param participation to be stopped
     */
    public void cleanupRepository(ProgrammingExerciseStudentParticipation participation) {
        // ignore participations without repository URL
        if (participation.getRepositoryUrl() != null) {
            versionControlService.get().deleteRepository(participation.getRepositoryUrlAsUrl());
            participation.setRepositoryUrl(null);
            participation.setInitializationState(InitializationState.FINISHED);
            save(participation);
        }
    }

    /**
     * Delete the participation by participationId.
     *
     * @param participationId  the participationId of the entity
     * @param deleteBuildPlan  determines whether the corresponding build plan should be deleted as well
     * @param deleteRepository determines whether the corresponding repository should be deleted as well
     */
    @Transactional
    public void delete(Long participationId, boolean deleteBuildPlan, boolean deleteRepository) {
        StudentParticipation participation = studentParticipationRepository.findWithEagerSubmissionsAndResultsById(participationId).get();
        log.debug("Request to delete Participation : {}", participation);

        if (participation instanceof ProgrammingExerciseStudentParticipation) {
            ProgrammingExerciseStudentParticipation programmingExerciseParticipation = (ProgrammingExerciseStudentParticipation) participation;
            if (deleteBuildPlan && programmingExerciseParticipation.getBuildPlanId() != null) {
                final var projectKey = programmingExerciseParticipation.getProgrammingExercise().getProjectKey();
                continuousIntegrationService.get().deleteBuildPlan(projectKey, programmingExerciseParticipation.getBuildPlanId());
            }
            if (deleteRepository && programmingExerciseParticipation.getRepositoryUrl() != null) {
                try {
                    versionControlService.get().deleteRepository(programmingExerciseParticipation.getRepositoryUrlAsUrl());
                }
                catch (Exception ex) {
                    log.error("Could not delete repository: " + ex.getMessage());
                }
            }

            // delete local repository cache
            try {
                if (programmingExerciseParticipation.getRepositoryUrlAsUrl() != null) {
                    gitService.deleteLocalRepository(programmingExerciseParticipation);
                }
            }
            catch (Exception ex) {
                log.error("Error while deleting local repository", ex);
            }
        }

        complaintResponseRepository.deleteByComplaint_Result_Participation_Id(participationId);
        complaintRepository.deleteByResult_Participation_Id(participationId);

        participation = (StudentParticipation) deleteResultsAndSubmissionsOfParticipation(participation.getId());

        Exercise exercise = participation.getExercise();
        exercise.removeParticipation(participation);
        exerciseRepository.save(exercise);
        studentParticipationRepository.delete(participation);
    }

    /**
     * Remove all results and submissions of the given participation. Will do nothing if invoked with a participation without results/submissions.
     *
     * @param participationId the id of the participation to delete results/submissions from.
     * @return participation without submissions and results.
     */
    @Transactional
    public Participation deleteResultsAndSubmissionsOfParticipation(Long participationId) {
        Participation participation = participationRepository.getOneWithEagerSubmissionsAndResults(participationId);
        // This is the default case: We delete results and submissions from direction result -> submission. This will only delete submissions that have a result.
        if (participation.getResults() != null) {
            for (Result result : participation.getResults()) {

                if (participation.getExercise() instanceof ModelingExercise) {
                    // The conflicting results referencing a result of the given participation need to be deleted to prevent Hibernate constraint violation errors.
                    conflictingResultService.deleteConflictingResultsByResultId(result.getId());
                }

                resultRepository.deleteById(result.getId());
                // The following code is necessary, because we might have submissions in results which are not properly connected to a participation and CASCASE_REMOVE is not
                // active in this case
                if (result.getSubmission() != null) {
                    Submission submissionToDelete = result.getSubmission();
                    submissionRepository.deleteById(submissionToDelete.getId());
                    result.setSubmission(null);
                    participation.removeSubmissions(submissionToDelete);
                }
            }
        }
        // The following case is necessary, because we might have submissions without a result.
        // At this point only submissions without a result will still be connected to the participation.
        if (participation.getSubmissions() != null) {
            for (Submission submission : participation.getSubmissions()) {
                submissionRepository.deleteById(submission.getId());
            }
        }
        return participation;
    }

    /**
     * Delete all participations belonging to the given exercise
     *
     * @param exerciseId the id of the exercise
     * @param deleteBuildPlan specify if build plan should be deleted
     * @param deleteRepository specify if repository should be deleted
     */
    @Transactional
    public void deleteAllByExerciseId(Long exerciseId, boolean deleteBuildPlan, boolean deleteRepository) {
        List<StudentParticipation> participationsToDelete = findByExerciseId(exerciseId);

        for (StudentParticipation participation : participationsToDelete) {
            delete(participation.getId(), deleteBuildPlan, deleteRepository);
        }
    }

    /**
     * Delete all participations belonging to the given team
     *
     * @param teamId the id of the team
     * @param deleteBuildPlan specify if build plan should be deleted
     * @param deleteRepository specify if repository should be deleted
     */
    @Transactional
    public void deleteAllByTeamId(Long teamId, boolean deleteBuildPlan, boolean deleteRepository) {
        log.info("Request to delete all participations of Team with id : {}", teamId);

        List<StudentParticipation> participationsToDelete = findByTeamId(teamId);

        for (StudentParticipation participation : participationsToDelete) {
            delete(participation.getId(), deleteBuildPlan, deleteRepository);
        }
    }

    /**
     * Get one participation with eager course.
     *
     * @param participationId id of the participation
     * @return participation with eager course
     */
    public StudentParticipation findOneWithEagerCourse(Long participationId) {
        return studentParticipationRepository.findOneByIdWithEagerExerciseAndEagerCourse(participationId);
    }

    /**
     * Get one participation with eager course.
     *
     * @param participationId id of the participation
     * @return participation with eager course
     */
    public StudentParticipation findOneWithEagerResultsAndCourse(Long participationId) {
        return studentParticipationRepository.findOneByIdWithEagerResultsAndExerciseAndEagerCourse(participationId);
    }

    /**
     * Check if a participation can be accessed with the current user.
     *
     * @param participation to access
     * @return can user access participation
     */
    public boolean canAccessParticipation(StudentParticipation participation) {
        return Optional.ofNullable(participation).isPresent() && userHasPermissions(participation);
    }

    /**
     * Check if a user has permissions to access a certain participation. This includes not only the owner of the participation but also the TAs and instructors of the course.
     *
     * @param participation to access
     * @return does user has permissions to access participation
     */
    private boolean userHasPermissions(StudentParticipation participation) {
        if (authCheckService.isOwnerOfParticipation(participation))
            return true;
        // if the user is not the owner of the participation, the user can only see it in case he is
        // a teaching assistant or an instructor of the course, or in case he is admin
        User user = userService.getUserWithGroupsAndAuthorities();
        Course course = participation.getExercise().getCourseViaExerciseGroupOrCourseMember();
        return authCheckService.isAtLeastTeachingAssistantInCourse(course, user);
    }

    /**
     * Get template exercise participation belonging to build plan.
     *
     * @param planKey the id of build plan
     * @return template exercise participation belonging to build plan
     */
    public Optional<TemplateProgrammingExerciseParticipation> findTemplateParticipationByBuildPlanId(String planKey) {
        return templateProgrammingExerciseParticipationRepository.findByBuildPlanIdWithResults(planKey);
    }

    /**
     * Get solution exercise participation belonging to build plan.
     *
     * @param planKey the id of build plan
     * @return solution exercise participation belonging to build plan
     */
    public Optional<SolutionProgrammingExerciseParticipation> findSolutionParticipationByBuildPlanId(String planKey) {
        return solutionProgrammingExerciseParticipationRepository.findByBuildPlanIdWithResults(planKey);
    }

    /**
     * Get all participations for the given student and individual-mode exercises combined with their submissions with a result
     *
     * @param studentId the id of the student for which the participations should be found
     * @param exercises the individual-mode exercises for which participations should be found
     * @return student's participations
     */
    public List<StudentParticipation> findByStudentIdAndIndividualExercisesWithEagerSubmissionsResult(Long studentId, List<Exercise> exercises) {
        return studentParticipationRepository.findByStudentIdAndIndividualExercisesWithEagerSubmissionsResult(studentId, exercises);
    }

    /**
     * Get all participations for the given student and team-mode exercises combined with their submissions with a result
     *
     * @param studentId the id of the student for which the participations should be found
     * @param exercises the team-mode exercises for which participations should be found
     * @return student's team participations
     */
    public List<StudentParticipation> findByStudentIdAndTeamExercisesWithEagerSubmissionsResult(Long studentId, List<Exercise> exercises) {
        return studentParticipationRepository.findByStudentIdAndTeamExercisesWithEagerSubmissionsResult(studentId, exercises);
    }

    /**
     * Get all participations for the given student and team-mode exercises combined with their submissions with a result
     *
     * @param courseId the id of the course for which the participations should be found
     * @param teamShortName the short name of the team for which participations should be found
     * @return participations of team
     */
    public List<StudentParticipation> findAllByCourseIdAndTeamShortNameWithEagerSubmissionsResult(long courseId, String teamShortName) {
        return studentParticipationRepository.findAllByCourseIdAndTeamShortNameWithEagerSubmissionsResult(courseId, teamShortName);
    }

    /**
     * Get a mapping of participation ids to the number of submission for each participation.
     *
     * @param exerciseId the id of the exercise for which to consider participations
     * @return the number of submissions per participation in the given exercise
     */
    public Map<Long, Integer> countSubmissionsPerParticipationByExerciseId(long exerciseId) {
        return convertListOfCountsIntoMap(studentParticipationRepository.countSubmissionsPerParticipationByExerciseId(exerciseId));
    }

    /**
     * Get a mapping of participation ids to the number of submission for each participation.
     *
     * @param courseId the id of the course for which to consider participations
     * @param teamShortName the short name of the team for which to consider participations
     * @return the number of submissions per participation in the given course for the team
     */
    public Map<Long, Integer> countSubmissionsPerParticipationByCourseIdAndTeamShortName(long courseId, String teamShortName) {
        return convertListOfCountsIntoMap(studentParticipationRepository.countSubmissionsPerParticipationByCourseIdAndTeamShortName(courseId, teamShortName));
    }

    /**
     * Converts List<[participationId, submissionCount]> into Map<participationId -> submissionCount>
     *
     * @param participationIdAndSubmissionCountPairs list of pairs (participationId, submissionCount)
     * @return map of participation id to submission count
     */
    private Map<Long, Integer> convertListOfCountsIntoMap(List<long[]> participationIdAndSubmissionCountPairs) {

        return participationIdAndSubmissionCountPairs.stream().collect(Collectors.toMap(participationIdAndSubmissionCountPair -> participationIdAndSubmissionCountPair[0], // participationId
                participationIdAndSubmissionCountPair -> Math.toIntExact(participationIdAndSubmissionCountPair[1]) // submissionCount
        ));
    }
}
