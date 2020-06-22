package de.tum.in.www1.artemis.service.scheduled;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.*;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;

import de.tum.in.www1.artemis.domain.*;
import de.tum.in.www1.artemis.domain.enumeration.AssessmentType;
import de.tum.in.www1.artemis.domain.enumeration.InitializationState;
import de.tum.in.www1.artemis.domain.enumeration.SubmissionType;
import de.tum.in.www1.artemis.domain.participation.StudentParticipation;
import de.tum.in.www1.artemis.domain.quiz.QuizExercise;
import de.tum.in.www1.artemis.domain.quiz.QuizSubmission;
import de.tum.in.www1.artemis.repository.QuizSubmissionRepository;
import de.tum.in.www1.artemis.repository.ResultRepository;
import de.tum.in.www1.artemis.repository.StudentParticipationRepository;
import de.tum.in.www1.artemis.service.QuizExerciseService;
import de.tum.in.www1.artemis.service.QuizStatisticService;
import de.tum.in.www1.artemis.service.UserService;

@Service
public class QuizScheduleService {

    private static final Logger log = LoggerFactory.getLogger(QuizScheduleService.class);

    /**
     * quizExerciseId -> Map<username -> QuizSubmission>
     */
    private static Map<Long, Map<String, QuizSubmission>> submissionHashMap = new ConcurrentHashMap<>();

    /**
     * quizExerciseId -> Map<username -> StudentParticipation>
     */
    private static Map<Long, Map<String, StudentParticipation>> participationHashMap = new ConcurrentHashMap<>();

    /**
     * quizExerciseId -> [Result]
     */
    private static Map<Long, Set<Result>> resultHashMap = new ConcurrentHashMap<>();

    /**
     * quizExerciseId -> ScheduledFuture
     */
    private static Map<Long, ScheduledFuture<?>> quizStartSchedules = new ConcurrentHashMap<>();

    private static Map<Long, QuizExercise> quizExerciseMap = new ConcurrentHashMap<>();

    private static ThreadPoolTaskScheduler threadPoolTaskScheduler;

    private ScheduledFuture<?> scheduledProcessQuizSubmissions;

    private final StudentParticipationRepository studentParticipationRepository;

    private final ResultRepository resultRepository;

    private final UserService userService;

    private final QuizSubmissionRepository quizSubmissionRepository;

    private QuizExerciseService quizExerciseService;

    private QuizStatisticService quizStatisticService;

    private SimpMessageSendingOperations messagingTemplate;

    public QuizScheduleService(SimpMessageSendingOperations messagingTemplate, StudentParticipationRepository studentParticipationRepository, ResultRepository resultRepository,
            UserService userService, QuizSubmissionRepository quizSubmissionRepository) {
        this.messagingTemplate = messagingTemplate;
        this.studentParticipationRepository = studentParticipationRepository;
        this.resultRepository = resultRepository;
        this.userService = userService;
        this.quizSubmissionRepository = quizSubmissionRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void applicationReady() {
        // activate Quiz Schedule Service
        startSchedule(5 * 1000);                          // every 5 seconds
    }

    @Autowired
    // break the dependency cycle
    public void setQuizExerciseService(QuizExerciseService quizExerciseService) {
        this.quizExerciseService = quizExerciseService;
    }

    @Autowired
    // break the dependency cycle
    public void setQuizStatisticService(QuizStatisticService quizStatisticService) {
        this.quizStatisticService = quizStatisticService;
    }

    /**
     * add a quizSubmission to the submissionHashMap
     *
     * @param quizExerciseId the quizExerciseId of the quiz the submission belongs to (first Key)
     * @param username       the username of the user, who submitted the submission (second Key)
     * @param quizSubmission the quizSubmission, which should be added (Value)
     */
    public static void updateSubmission(Long quizExerciseId, String username, QuizSubmission quizSubmission) {

        if (quizSubmission != null && quizExerciseId != null && username != null) {
            // check if there is already a quizSubmission with the same quiz
            if (!submissionHashMap.containsKey(quizExerciseId)) {
                submissionHashMap.put(quizExerciseId, new ConcurrentHashMap<>());
            }
            submissionHashMap.get(quizExerciseId).put(username, quizSubmission);
        }
    }

    /**
     * add a result to resultHashMap for a statistic-update
     * this should only be invoked once, when the quiz was submitted
     *
     * @param quizExerciseId the quizExerciseId of the quiz the result belongs to (first Key)
     * @param result the result, which should be added
     */
    public static void addResultForStatisticUpdate(Long quizExerciseId, Result result) {
        log.debug("add result for statistic update for quiz " + quizExerciseId + ": " + result);
        if (quizExerciseId != null && result != null) {
            // check if there is already a result with the same quiz
            if (!resultHashMap.containsKey(quizExerciseId)) {
                resultHashMap.put(quizExerciseId, new HashSet<>());
            }
            resultHashMap.get(quizExerciseId).add(result);
        }
    }

    /**
     * add a participation to participationHashMap to send them back to the user when the quiz ends
     *
     * @param quizExerciseId        the quizExerciseId of the quiz the result belongs to (first Key)
     * @param participation the result, which should be added
     */
    private static void addParticipation(Long quizExerciseId, StudentParticipation participation) {

        if (quizExerciseId != null && participation != null) {
            // check if there is already a result with the same quiz
            if (!participationHashMap.containsKey(quizExerciseId)) {
                participationHashMap.put(quizExerciseId, new ConcurrentHashMap<>());
            }
            participationHashMap.get(quizExerciseId).put(participation.getParticipantIdentifier(), participation);
        }
    }

    /**
     * get a quizSubmission from the submissionHashMap by quizExerciseId and username
     *
     * @param quizExerciseId   the quizExerciseId of the quiz the submission belongs to (first Key)
     * @param username the username of the user, who submitted the submission (second Key)
     * @return the quizSubmission, with the given quizExerciseId and username -> return an empty QuizSubmission if there is no quizSubmission -> return null if the quizExerciseId or if the
     *         username is null
     */
    public static QuizSubmission getQuizSubmission(Long quizExerciseId, String username) {

        if (quizExerciseId == null || username == null) {
            return null;
        }
        QuizSubmission quizSubmission;
        // check if the the map contains submissions with the quizExerciseId
        if (submissionHashMap.containsKey(quizExerciseId)) {
            // return the quizSubmission with the username-Key
            quizSubmission = submissionHashMap.get(quizExerciseId).get(username);
            if (quizSubmission != null) {
                return quizSubmission;
            }
        }
        // return an empty quizSubmission if the maps contain no mapping for the keys
        return new QuizSubmission().submittedAnswers(new HashSet<>());
    }

    /**
     * get a participation from the participationHashMap by quizExerciseId and username
     *
     * @param quizExerciseId   the quizExerciseId of the quiz, the participation belongs to (first Key)
     * @param username the username of the user, the participation belongs to (second Key)
     * @return the participation with the given quizExerciseId and username -> return null if there is no participation -> return null if the quizExerciseId or if the username is null
     */
    public static StudentParticipation getParticipation(Long quizExerciseId, String username) {
        if (quizExerciseId == null || username == null) {
            return null;
        }
        // check if the the map contains participations with the quizExerciseId
        if (participationHashMap.containsKey(quizExerciseId)) {
            // return the participation with the username-Key
            return participationHashMap.get(quizExerciseId).get(username);
        }
        // return null if the maps contain no mapping for the keys
        return null;
    }

    public static QuizExercise getQuizExercise(Long quizExerciseId) {
        if (quizExerciseId == null) {
            return null;
        }
        return quizExerciseMap.get(quizExerciseId);
    }

    /**
     * stores the quiz exercise in a HashMap for faster retrieval during the quiz
     * @param quizExercise should include questions and statistics without Hibernate proxies!
     */
    public static void updateQuizExercise(QuizExercise quizExercise) {
        log.debug("Quiz exercise {} updated in quiz exercise map: {}", quizExercise.getId(), quizExercise);
        quizExerciseMap.put(quizExercise.getId(), quizExercise);
    }

    /**
     * Start scheduler of quiz schedule service
     *
     * @param delayInMillis gap for which the QuizScheduleService should run repeatedly
     */
    public void startSchedule(long delayInMillis) {
        if (threadPoolTaskScheduler == null) {
            threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
            threadPoolTaskScheduler.setThreadNamePrefix("QuizScheduler");
            threadPoolTaskScheduler.setPoolSize(1);
            threadPoolTaskScheduler.initialize();
            scheduledProcessQuizSubmissions = threadPoolTaskScheduler.scheduleWithFixedDelay(this::processCachedQuizSubmissions, delayInMillis);
            log.info("QuizScheduleService was started to run repeatedly with {} second delay.", delayInMillis / 1000.0);

            // schedule quiz start for all existing quizzes that are planned to start in the future
            List<QuizExercise> quizExercises = quizExerciseService.findAllPlannedToStartInTheFuture();
            log.info("Found {} quiz exercises with planned start in the future", quizExercises.size());
            for (QuizExercise quizExercise : quizExercises) {
                // Note: the quiz exercise does not include questions and statistics, so we pass the id
                scheduleQuizStart(quizExercise.getId());
            }
        }
        else {
            log.debug("Cannot start quiz exercise schedule service, it is already RUNNING");
        }
    }

    /**
     * stop scheduler (interrupts if running)
     */
    public void stopSchedule() {
        if (threadPoolTaskScheduler != null) {
            log.info("Try to stop quiz schedule service");

            if (scheduledProcessQuizSubmissions != null && !scheduledProcessQuizSubmissions.isCancelled()) {
                boolean cancelSuccess = scheduledProcessQuizSubmissions.cancel(true);
                log.info("Stop Quiz Schedule Service was successful: " + cancelSuccess);
                scheduledProcessQuizSubmissions = null;
            }
            for (Long quizExerciseId : quizStartSchedules.keySet()) {
                cancelScheduledQuizStart(quizExerciseId);
            }
            threadPoolTaskScheduler.shutdown();
            threadPoolTaskScheduler = null;
        }
        else {
            log.debug("Cannot stop quiz exercise schedule service, it was already STOPPED");
        }
    }

    /**
     * Start scheduler of quiz and update the quiz exercise in the hash map
     *
     * @param quizExerciseId the id of the quiz exercise that should be scheduled for being started automatically
     */
    public void scheduleQuizStart(final long quizExerciseId) {
        // first remove and cancel old scheduledFuture if it exists
        cancelScheduledQuizStart(quizExerciseId);
        // reload from database to make sure there are no proxy objects
        final var quizExercise = quizExerciseService.findOneWithQuestionsAndStatistics(quizExerciseId);
        updateQuizExercise(quizExercise);

        if (quizExercise.isIsPlannedToStart() && quizExercise.getReleaseDate().isAfter(ZonedDateTime.now())) {
            // schedule sending out filtered quiz over websocket
            ScheduledFuture<?> scheduledFuture = threadPoolTaskScheduler.schedule(() -> quizExerciseService.sendQuizExerciseToSubscribedClients(quizExercise, "start-now"),
                    Date.from(quizExercise.getReleaseDate().toInstant()));

            // save scheduled future in HashMap
            quizStartSchedules.put(quizExercise.getId(), scheduledFuture);
        }
    }

    /**
     * cancels the quiz start for the given exercise id, e.g. because the quiz was deleted or the quiz start date was changed
     *
     * @param quizExerciseId the quiz exercise for which the quiz start should be canceled
     */
    public void cancelScheduledQuizStart(Long quizExerciseId) {
        ScheduledFuture<?> scheduledFuture = quizStartSchedules.remove(quizExerciseId);
        if (scheduledFuture != null) {
            boolean cancelSuccess = scheduledFuture.cancel(true);
            log.info("Stop scheduled quiz start for quiz " + quizExerciseId + " was successful: " + cancelSuccess);
        }
        quizExerciseMap.remove(quizExerciseId);
    }

    /*
     * Clears all quiz data for all quiz exercises from the 4 hash maps for quizzes
     */
    public void clearAllQuizData() {
        participationHashMap.clear();
        submissionHashMap.clear();
        resultHashMap.clear();
        quizExerciseMap.clear();
    }

    /**
     * Clears all quiz data for one specific quiz exercise from the 4 hash maps for quizzes
     * @param quizExerciseId refers to one specific quiz exercise for which the data should be cleared
     */
    public void clearQuizData(Long quizExerciseId) {
        // delete all participation, submission, and result hashmap entries that correspond to this quiz
        participationHashMap.remove(quizExerciseId);
        submissionHashMap.remove(quizExerciseId);
        resultHashMap.remove(quizExerciseId);
        quizExerciseMap.remove(quizExerciseId);
    }

    /**
     * // @formatter:off
     * 1. Check SubmissionHashMap for new submissions with “isSubmitted() == true”
     *      a. Process each Submission (set submissionType to “SubmissionType.MANUAL”) and create Participation and Result and save them to Database (DB WRITE)
     *      b. Remove processed Submissions from SubmissionHashMap and write Participation with Result into ParticipationHashMap and write Result into ResultHashMap
     * 2. If Quiz has ended:
     *      a. Process all Submissions in SubmissionHashMap that belong to this quiz i. set “isSubmitted” to “true” and submissionType to “SubmissionType.TIMEOUT”
     *          ii. Create Participation and Result and save to Database (DB WRITE)
     *          iii. Remove processed Submissions from SubmissionHashMap and write Participations with Result into ParticipationHashMap and Results into ResultHashMap
     *      b. Send out Participations (including QuizExercise and Result) from ParticipationHashMap to each participant and remove them from ParticipationHashMap (WEBSOCKET SEND)
     * 3. Update Statistics with Results from ResultHashMap (DB READ and DB WRITE) and remove from ResultHashMap
     * 4. Send out new Statistics to instructors (WEBSOCKET SEND)
     */
    public void processCachedQuizSubmissions() {
        log.debug("Process cached quiz submissions");
        // global try-catch for error logging
        try {
            long start = System.nanoTime();

            // create Participations and Results if the submission was submitted or if the quiz has ended and save them to Database (DB Write)
            for (long quizExerciseId : submissionHashMap.keySet()) {

                QuizExercise quizExercise = quizExerciseService.findOneWithQuestions(quizExerciseId);
                // check if quiz has been deleted
                if (quizExercise == null) {
                    submissionHashMap.remove(quizExerciseId);
                    continue;
                }

                // if quiz has ended, all submissions will be processed => we can remove the inner HashMap for this quiz
                // if quiz hasn't ended, some submissions (those that are not submitted) will stay in HashMap => keep inner HashMap
                Map<String, QuizSubmission> submissions;
                if (quizExercise.isEnded()) {
                    submissions = submissionHashMap.remove(quizExerciseId);
                }
                else {
                    submissions = submissionHashMap.get(quizExerciseId);
                }

                int numberOfSubmittedSubmissions = saveQuizSubmissionWithParticipationAndResultToDatabase(quizExercise, submissions);

                if (numberOfSubmittedSubmissions > 0) {
                    log.info("Saved {} submissions to database in {} in quiz {}", numberOfSubmittedSubmissions, printDuration(start), quizExercise.getTitle());
                }
            }

            start = System.nanoTime();
            // Send out Participations from ParticipationHashMap to each user if the quiz has ended
            for (long quizExerciseId : participationHashMap.keySet()) {

                // get the quiz exercise with questions but without the statistics from the database
                QuizExercise quizExercise = quizExerciseService.findOneWithQuestions(quizExerciseId);
                // check if quiz has been deleted
                if (quizExercise == null) {
                    participationHashMap.remove(quizExerciseId);
                    continue;
                }

                // check if the quiz has ended
                if (quizExercise.isEnded()) {
                    // send the participation with containing result and quiz back to the users via websocket and remove the participation from the ParticipationHashMap
                    Collection<StudentParticipation> finishedParticipations = participationHashMap.remove(quizExerciseId).values();
                    // TODO: use an executor service with e.g. X parallel threads
                    finishedParticipations.parallelStream().forEach(participation -> {
                        if (participation.getParticipant() == null || participation.getParticipantIdentifier() == null) {
                            log.error("Participation is missing student (or student is missing username): {}", participation);
                        }
                        else {
                            sendQuizResultToUser(quizExerciseId, participation);
                        }
                    });
                    if (finishedParticipations.size() > 0) {
                        log.info("Sent out {} participations in {} for quiz {}", finishedParticipations.size(), printDuration(start), quizExercise.getTitle());
                    }
                }
            }

            start = System.nanoTime();
            // Update Statistics with Results from ResultHashMap (DB Read and DB Write) and remove from ResultHashMap
            for (long quizExerciseId : resultHashMap.keySet()) {

                // get the quiz exercise with the statistic from the database
                QuizExercise quizExercise = quizExerciseService.findOneWithQuestionsAndStatistics(quizExerciseId);
                // check if quiz has been deleted (edge case), then do nothing!
                if (quizExercise == null) {
                    log.debug("Remove quiz " + quizExerciseId + " from resultHashMap");
                    resultHashMap.remove(quizExerciseId);
                    continue;
                }

                // update statistic with all results of the quizExercise
                try {
                    Set<Result> newResultsForQuiz = resultHashMap.remove(quizExerciseId);
                    quizStatisticService.updateStatistics(newResultsForQuiz, quizExercise);
                    log.info("Updated statistics with {} new results in {} for quiz {}", newResultsForQuiz.size(), printDuration(start), quizExercise.getTitle());
                }
                catch (Exception e) {
                    log.error("Exception in StatisticService.updateStatistics(): {}", e.getMessage(), e);
                }
            }
        }
        catch (Exception e) {
            log.error("Exception in Quiz Schedule: {}", e.getMessage(), e);
        }
    }

    private static String printDuration(long timeNanoStart) {
        long durationInMicroSeconds = (System.nanoTime() - timeNanoStart) / 1000;
        if (durationInMicroSeconds > 1000) {
            double durationInMilliSeconds = durationInMicroSeconds / 1000.0;
            if (durationInMilliSeconds > 1000) {
                double durationInSeconds = durationInMilliSeconds / 1000.0;
                return roundOffTo2DecPlaces(durationInSeconds) + "s";
            }
            return roundOffTo2DecPlaces(durationInMilliSeconds) + "ms";
        }
        return durationInMicroSeconds + "µs";
    }

    private static String roundOffTo2DecPlaces(double val) {
        return String.format("%.2f", val);
    }

    private void sendQuizResultToUser(long quizExerciseId, StudentParticipation participation) {
        var user = participation.getParticipantIdentifier();
        removeUnnecessaryObjectsBeforeSendingToClient(participation);
        messagingTemplate.convertAndSendToUser(user, "/topic/exercise/" + quizExerciseId + "/participation", participation);
    }

    private void removeUnnecessaryObjectsBeforeSendingToClient(StudentParticipation participation) {
        if (participation.getExercise() != null) {
            var quizExercise = (QuizExercise) participation.getExercise();
            // we do not need the course and lectures
            quizExercise.setCourse(null);
            // students should not see statistics
            // TODO: this would be useful, but leads to problems when the quiz schedule service wants to access the statistics again later on
            // quizExercise.setQuizPointStatistic(null);
            // quizExercise.getQuizQuestions().forEach(quizQuestion -> quizQuestion.setQuizQuestionStatistic(null));
        }
        // submissions are part of results, so we do not need them twice
        participation.setSubmissions(null);
        participation.setParticipant(null);
        if (participation.getResults() != null && participation.getResults().size() > 0) {
            QuizSubmission quizSubmission = (QuizSubmission) participation.getResults().iterator().next().getSubmission();
            if (quizSubmission != null && quizSubmission.getSubmittedAnswers() != null) {
                for (SubmittedAnswer submittedAnswer : quizSubmission.getSubmittedAnswers()) {
                    if (submittedAnswer.getQuizQuestion() != null) {
                        // we do not need all information of the questions again, they are already stored in the exercise
                        var question = submittedAnswer.getQuizQuestion();
                        submittedAnswer.setQuizQuestion(question.copyQuestionId());
                    }
                }
            }
        }
    }

    /**
     * check if the user submitted the submission or if the quiz has ended: if true: -> Create Participation and Result and save to Database (DB Write) Remove processed Submissions
     * from SubmissionHashMap and write Participations with Result into ParticipationHashMap and Results into ResultHashMap
     *
     * @param quizExercise      the quiz which should be checked
     * @param userSubmissionMap a Map with all submissions for the given quizExercise mapped by the username
     * @return                  the number of processed submissions (submit or timeout)
     */
    private int saveQuizSubmissionWithParticipationAndResultToDatabase(@NotNull QuizExercise quizExercise, Map<String, QuizSubmission> userSubmissionMap) {

        int count = 0;

        for (String username : userSubmissionMap.keySet()) {
            try {
                // first case: the user submitted the quizSubmission
                QuizSubmission quizSubmission = userSubmissionMap.get(username);
                if (quizSubmission.isSubmitted()) {
                    if (quizSubmission.getType() == null) {
                        quizSubmission.setType(SubmissionType.MANUAL);
                    }
                } // second case: the quiz has ended
                else if (quizExercise.isEnded()) {
                    quizSubmission.setSubmitted(true);
                    quizSubmission.setType(SubmissionType.TIMEOUT);
                    quizSubmission.setSubmissionDate(ZonedDateTime.now());
                }
                else {
                    // the quiz is running and the submission was not yet submitted.
                    continue;
                }

                count++;
                // Create Participation and Result and save to Database (DB Write)
                // Remove processed Submissions from SubmissionHashMap and write Participations with Result into ParticipationHashMap and Results into ResultHashMap

                StudentParticipation participation = new StudentParticipation();
                // TODO: when this is set earlier for the individual quiz start of a student, we don't need to set this here anymore
                participation.setInitializationDate(quizSubmission.getSubmissionDate());
                Optional<User> user = userService.getUserByLogin(username);
                user.ifPresent(participation::setParticipant);
                // add the quizExercise to the participation
                participation.setExercise(quizExercise);
                participation.setInitializationState(InitializationState.FINISHED);

                // create new result
                Result result = new Result().participation(participation).submission(quizSubmission);
                result.setRated(true);
                result.setAssessmentType(AssessmentType.AUTOMATIC);
                result.setCompletionDate(quizSubmission.getSubmissionDate());
                result.setSubmission(quizSubmission);

                // calculate scores and update result and submission accordingly
                quizSubmission.calculateAndUpdateScores(quizExercise);
                result.evaluateSubmission();

                // add result to participation
                participation.addResult(result);

                // add submission to participation
                participation.addSubmissions(quizSubmission);

                // NOTE: we save participation, submission and result here individually so that one exception (e.g. duplicated key) cannot destroy multiple student answers
                participation = studentParticipationRepository.save(participation);
                quizSubmissionRepository.save(quizSubmission);
                result = resultRepository.save(result);

                // add the participation to the participationHashMap for the send out at the end of the quiz
                addParticipation(quizExercise.getId(), participation);

                // remove the submission only after the participation has been added to the participation hashmap to avoid duplicated key exceptions for multiple participations for
                // the same user
                userSubmissionMap.remove(username);

                // add the result of the participation resultHashMap for the statistic-Update
                addResultForStatisticUpdate(quizExercise.getId(), result);
            }
            catch (Exception e) {
                log.error("Exception in saveQuizSubmissionWithParticipationAndResultToDatabase() for user {} in quiz {}: {}", username, quizExercise.getId(), e.getMessage(), e);
            }
        }

        return count;
    }
}
