package de.tum.in.www1.artemis.service;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.tum.in.www1.artemis.domain.Result;
import de.tum.in.www1.artemis.domain.SubmittedAnswer;
import de.tum.in.www1.artemis.domain.enumeration.AssessmentType;
import de.tum.in.www1.artemis.domain.enumeration.SubmissionType;
import de.tum.in.www1.artemis.domain.participation.Participation;
import de.tum.in.www1.artemis.domain.quiz.QuizExercise;
import de.tum.in.www1.artemis.domain.quiz.QuizSubmission;
import de.tum.in.www1.artemis.exception.QuizSubmissionException;
import de.tum.in.www1.artemis.repository.QuizSubmissionRepository;
import de.tum.in.www1.artemis.repository.ResultRepository;
import de.tum.in.www1.artemis.service.scheduled.QuizScheduleService;

@Service
public class QuizSubmissionService {

    private final Logger log = LoggerFactory.getLogger(QuizSubmissionService.class);

    private final QuizSubmissionRepository quizSubmissionRepository;

    private final ResultRepository resultRepository;

    private QuizExerciseService quizExerciseService;

    private ParticipationService participationService;

    public QuizSubmissionService(QuizSubmissionRepository quizSubmissionRepository, ResultRepository resultRepository) {
        this.quizSubmissionRepository = quizSubmissionRepository;
        this.resultRepository = resultRepository;
    }

    @Autowired
    // break the dependency cycle
    public void setQuizExerciseService(QuizExerciseService quizExerciseService) {
        this.quizExerciseService = quizExerciseService;
    }

    @Autowired
    // break the dependency cycle
    public void setParticipationService(ParticipationService participationService) {
        this.participationService = participationService;
    }

    public QuizSubmission findOne(Long id) {
        return quizSubmissionRepository.findById(id).get();
    }

    public List<QuizSubmission> findAll() {
        return quizSubmissionRepository.findAll();
    }

    public void delete(Long id) {
        quizSubmissionRepository.deleteById(id);
    }

    /**
     * Submit the given submission for practice
     *
     * @param quizSubmission the submission to submit
     * @param quizExercise   the exercise to submit in
     * @param participation  the participation where the result should be saved
     * @return the result entity
     */
    public Result submitForPractice(QuizSubmission quizSubmission, QuizExercise quizExercise, Participation participation) {
        // update submission properties
        quizSubmission.setSubmitted(true);
        quizSubmission.setType(SubmissionType.MANUAL);
        quizSubmission.setSubmissionDate(ZonedDateTime.now());

        // calculate scores
        quizSubmission.calculateAndUpdateScores(quizExercise);

        // create and save result
        Result result = new Result().participation(participation);
        result = resultRepository.save(result);
        result.setSubmission(quizSubmission);
        result.setRated(false);
        result.setAssessmentType(AssessmentType.AUTOMATIC);
        result.setCompletionDate(ZonedDateTime.now());
        // calculate score and update result accordingly
        result.evaluateSubmission();
        // save result
        quizSubmission.setResult(result);
        quizSubmission.setParticipation(participation);
        quizSubmissionRepository.save(quizSubmission);
        result = resultRepository.save(result);

        // result.submission and result.participation.exercise.quizQuestions turn into proxy objects after saving, so we need to set it again to prevent problems later on
        result.setSubmission(quizSubmission);
        result.setParticipation(participation);

        // add result to statistics
        QuizScheduleService.addResultForStatisticUpdate(quizExercise.getId(), result);
        log.debug("submit practice quiz finished: " + quizSubmission);
        return result;
    }

    /**
     * Saves a quiz submission into the hash maps for live quizzes. Submitted quizzes are marked to be saved into the database in the QuizScheduleService
     *
     * @param exerciseId the exerciseID to the corresponding QuizExercise
     * @param quizSubmission the submission which should be saved
     * @param username the username of the user who has initiated the request
     * @param submitted whether the user has pressed the submit button or not
     *
     * @return the updated quiz submission object
     * @throws QuizSubmissionException handles errors, e.g. when the live quiz has already ended, or when the quiz was already submitted before
     */
    public QuizSubmission saveSubmissionForLiveMode(Long exerciseId, QuizSubmission quizSubmission, String username, boolean submitted) throws QuizSubmissionException {

        // TODO: what happens if a user executes this call twice in the same moment (using 2 threads)

        String logText = submitted ? "submit quiz in live mode:" : "save quiz in live mode:";

        long start = System.nanoTime();
        // check if submission is still allowed
        QuizExercise quizExercise = QuizScheduleService.getQuizExercise(exerciseId);
        if (quizExercise == null) {
            // Fallback solution
            Optional<QuizExercise> optionalQuizExercise = quizExerciseService.findById(exerciseId);
            if (optionalQuizExercise.isEmpty()) {
                log.warn(logText + "Could not executre for user {} in quiz {} because the quizExercise could not be found.", username, exerciseId);
                throw new QuizSubmissionException("The quiz could not be found");
            }
            quizExercise = optionalQuizExercise.get();
        }
        log.debug(logText + "Received quiz exercise for user {} in quiz {} in {} µs.", username, exerciseId, (System.nanoTime() - start) / 1000);
        if (!quizExercise.isSubmissionAllowed()) {
            throw new QuizSubmissionException("The quiz is not active");
        }

        // TODO: add one additional check: fetch quizSubmission.getId() with the corresponding participation and check that the user of participation is the
        // same as the user who executes this call. This prevents injecting submissions to other users

        // check if user already submitted for this quiz
        Participation participation = participationService.participationForQuizWithResult(quizExercise, username);
        log.debug(logText + "Received participation for user {} in quiz {} in {} µs.", username, exerciseId, (System.nanoTime() - start) / 1000);
        if (!participation.getResults().isEmpty()) {
            log.debug("Participation for user {} in quiz {} has results", username, exerciseId);
            // NOTE: At this point, there can only be one Result because we already checked
            // if the quiz is active, so there is no way the student could have already practiced
            Result result = participation.getResults().iterator().next();
            if (result.getSubmission().isSubmitted()) {
                throw new QuizSubmissionException("You have already submitted the quiz");
            }
        }

        // recreate pointers back to submission in each submitted answer
        for (SubmittedAnswer submittedAnswer : quizSubmission.getSubmittedAnswers()) {
            submittedAnswer.setSubmission(quizSubmission);
        }

        // set submission date
        quizSubmission.setSubmissionDate(ZonedDateTime.now());

        // save submission to HashMap
        QuizScheduleService.updateSubmission(exerciseId, username, quizSubmission);

        log.info(logText + "Saved quiz submission for user {} in quiz {} after {} µs ", username, exerciseId, (System.nanoTime() - start) / 1000);
        return quizSubmission;
    }
}
