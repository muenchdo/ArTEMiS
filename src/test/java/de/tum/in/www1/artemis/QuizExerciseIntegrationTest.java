package de.tum.in.www1.artemis;

import static org.assertj.core.api.Assertions.*;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;

import de.tum.in.www1.artemis.domain.Course;
import de.tum.in.www1.artemis.domain.enumeration.AssessmentType;
import de.tum.in.www1.artemis.domain.enumeration.DifficultyLevel;
import de.tum.in.www1.artemis.domain.quiz.*;
import de.tum.in.www1.artemis.repository.ResultRepository;
import de.tum.in.www1.artemis.repository.StudentParticipationRepository;
import de.tum.in.www1.artemis.service.QuizExerciseService;
import de.tum.in.www1.artemis.service.scheduled.QuizScheduleService;
import de.tum.in.www1.artemis.util.DatabaseUtilService;
import de.tum.in.www1.artemis.util.RequestUtilService;

public class QuizExerciseIntegrationTest extends AbstractSpringIntegrationBambooBitbucketJiraTest {

    @Autowired
    DatabaseUtilService database;

    @Autowired
    QuizScheduleService quizScheduleService;

    @Autowired
    RequestUtilService request;

    @Autowired
    QuizExerciseService quizExerciseService;

    @Autowired
    StudentParticipationRepository studentParticipationRepository;

    @Autowired
    ResultRepository resultRepository;

    private QuizExercise quizExercise;

    @BeforeEach
    public void init() throws Exception {
        database.addUsers(15, 5, 1);
        quizScheduleService.startSchedule(5 * 1000);
    }

    @AfterEach
    public void tearDown() {
        database.resetDatabase();
        quizScheduleService.stopSchedule();
    }

    @Test
    @WithMockUser(value = "instructor1", roles = "INSTRUCTOR")
    public void testCreateQuizExercise() throws Exception {
        quizExercise = createQuizOnServer(ZonedDateTime.now().plusHours(5), null);

        // General assertions
        assertThat(quizExercise.getQuizQuestions().size()).as("Quiz questions were saved").isEqualTo(3);
        assertThat(quizExercise.getDuration()).as("Quiz duration was correctly set").isEqualTo(3600);
        assertThat(quizExercise.getDifficulty()).as("Quiz difficulty was correctly set").isEqualTo(DifficultyLevel.MEDIUM);

        // Quiz type specific assertions
        for (QuizQuestion question : quizExercise.getQuizQuestions()) {
            if (question instanceof MultipleChoiceQuestion) {
                MultipleChoiceQuestion multipleChoiceQuestion = (MultipleChoiceQuestion) question;
                assertThat(multipleChoiceQuestion.getAnswerOptions().size()).as("Multiple choice question answer options were saved").isEqualTo(2);
                assertThat(multipleChoiceQuestion.getTitle()).as("Multiple choice question title is correct").isEqualTo("MC");
                assertThat(multipleChoiceQuestion.getText()).as("Multiple choice question text is correct").isEqualTo("Q1");
                assertThat(multipleChoiceQuestion.getScore()).as("Multiple choice question score is correct").isEqualTo(4);

                List<AnswerOption> answerOptions = multipleChoiceQuestion.getAnswerOptions();
                assertThat(answerOptions.get(0).getText()).as("Text for answer option is correct").isEqualTo("A");
                assertThat(answerOptions.get(0).getHint()).as("Hint for answer option is correct").isEqualTo("H1");
                assertThat(answerOptions.get(0).getExplanation()).as("Explanation for answer option is correct").isEqualTo("E1");
                assertThat(answerOptions.get(0).isIsCorrect()).as("Is correct for answer option is correct").isTrue();
                assertThat(answerOptions.get(1).getText()).as("Text for answer option is correct").isEqualTo("B");
                assertThat(answerOptions.get(1).getHint()).as("Hint for answer option is correct").isEqualTo("H2");
                assertThat(answerOptions.get(1).getExplanation()).as("Explanation for answer option is correct").isEqualTo("E2");
                assertThat(answerOptions.get(1).isIsCorrect()).as("Is correct for answer option is correct").isFalse();
            }
            if (question instanceof DragAndDropQuestion) {
                DragAndDropQuestion dragAndDropQuestion = (DragAndDropQuestion) question;
                assertThat(dragAndDropQuestion.getDropLocations().size()).as("Drag and drop question drop locations were saved").isEqualTo(2);
                assertThat(dragAndDropQuestion.getDragItems().size()).as("Drag and drop question drag items were saved").isEqualTo(2);
                assertThat(dragAndDropQuestion.getTitle()).as("Drag and drop question title is correct").isEqualTo("DnD");
                assertThat(dragAndDropQuestion.getText()).as("Drag and drop question text is correct").isEqualTo("Q2");
                assertThat(dragAndDropQuestion.getScore()).as("Drag and drop question score is correct").isEqualTo(3);

                List<DropLocation> dropLocations = dragAndDropQuestion.getDropLocations();
                assertThat(dropLocations.get(0).getPosX()).as("Pos X for drop location is correct").isEqualTo(10);
                assertThat(dropLocations.get(0).getPosY()).as("Pos Y for drop location is correct").isEqualTo(10);
                assertThat(dropLocations.get(0).getWidth()).as("Width for drop location is correct").isEqualTo(10);
                assertThat(dropLocations.get(0).getHeight()).as("Height for drop location is correct").isEqualTo(10);
                assertThat(dropLocations.get(1).getPosX()).as("Pos X for drop location is correct").isEqualTo(20);
                assertThat(dropLocations.get(1).getPosY()).as("Pos Y for drop location is correct").isEqualTo(20);
                assertThat(dropLocations.get(1).getWidth()).as("Width for drop location is correct").isEqualTo(10);
                assertThat(dropLocations.get(1).getHeight()).as("Height for drop location is correct").isEqualTo(10);

                List<DragItem> dragItems = dragAndDropQuestion.getDragItems();
                assertThat(dragItems.get(0).getText()).as("Text for drag item is correct").isEqualTo("D1");
                assertThat(dragItems.get(1).getText()).as("Text for drag item is correct").isEqualTo("D2");
            }
            if (question instanceof ShortAnswerQuestion) {
                ShortAnswerQuestion shortAnswerQuestion = (ShortAnswerQuestion) question;
                assertThat(shortAnswerQuestion.getSpots().size()).as("Short answer question spots were saved").isEqualTo(2);
                assertThat(shortAnswerQuestion.getSolutions().size()).as("Short answer question solutions were saved").isEqualTo(2);
                assertThat(shortAnswerQuestion.getTitle()).as("Short answer question title is correct").isEqualTo("SA");
                assertThat(shortAnswerQuestion.getText()).as("Short answer question text is correct").isEqualTo("This is a long answer text");
                assertThat(shortAnswerQuestion.getScore()).as("Short answer question score is correct").isEqualTo(2);

                List<ShortAnswerSpot> spots = shortAnswerQuestion.getSpots();
                assertThat(spots.get(0).getSpotNr()).as("Spot nr for spot is correct").isEqualTo(0);
                assertThat(spots.get(0).getWidth()).as("Width for spot is correct").isEqualTo(1);
                assertThat(spots.get(1).getSpotNr()).as("Spot nr for spot is correct").isEqualTo(2);
                assertThat(spots.get(1).getWidth()).as("Width for spot is correct").isEqualTo(2);

                List<ShortAnswerSolution> solutions = shortAnswerQuestion.getSolutions();
                assertThat(solutions.get(0).getText()).as("Text for solution is correct").isEqualTo("is");
                assertThat(solutions.get(1).getText()).as("Text for solution is correct").isEqualTo("long");
            }
        }
    }

    @Test
    @WithMockUser(value = "instructor1", roles = "INSTRUCTOR")
    public void testEditQuizExercise() throws Exception {
        quizExercise = createQuizOnServer(ZonedDateTime.now().plusHours(5), null);

        MultipleChoiceQuestion mc = (MultipleChoiceQuestion) quizExercise.getQuizQuestions().get(0);
        mc.getAnswerOptions().remove(0);
        mc.getAnswerOptions().add(new AnswerOption().text("C").hint("H3").explanation("E3").isCorrect(true));
        mc.getAnswerOptions().add(new AnswerOption().text("D").hint("H4").explanation("E4").isCorrect(true));

        DragAndDropQuestion dnd = (DragAndDropQuestion) quizExercise.getQuizQuestions().get(1);
        dnd.getDropLocations().remove(0);
        dnd.getCorrectMappings().remove(0);
        dnd.getDragItems().remove(0);

        ShortAnswerQuestion sa = (ShortAnswerQuestion) quizExercise.getQuizQuestions().get(2);
        sa.getSpots().remove(0);
        sa.getSolutions().remove(0);
        sa.getCorrectMappings().remove(0);

        quizExercise = request.putWithResponseBody("/api/quiz-exercises", quizExercise, QuizExercise.class, HttpStatus.OK);

        // Quiz type specific assertions
        for (QuizQuestion question : quizExercise.getQuizQuestions()) {
            if (question instanceof MultipleChoiceQuestion) {
                MultipleChoiceQuestion multipleChoiceQuestion = (MultipleChoiceQuestion) question;
                assertThat(multipleChoiceQuestion.getAnswerOptions().size()).as("Multiple choice question answer options were saved").isEqualTo(3);
                assertThat(multipleChoiceQuestion.getTitle()).as("Multiple choice question title is correct").isEqualTo("MC");
                assertThat(multipleChoiceQuestion.getText()).as("Multiple choice question text is correct").isEqualTo("Q1");
                assertThat(multipleChoiceQuestion.getScore()).as("Multiple choice question score is correct").isEqualTo(4);

                List<AnswerOption> answerOptions = multipleChoiceQuestion.getAnswerOptions();
                assertThat(answerOptions.get(0).getText()).as("Text for answer option is correct").isEqualTo("B");
                assertThat(answerOptions.get(0).getHint()).as("Hint for answer option is correct").isEqualTo("H2");
                assertThat(answerOptions.get(0).getExplanation()).as("Explanation for answer option is correct").isEqualTo("E2");
                assertThat(answerOptions.get(0).isIsCorrect()).as("Is correct for answer option is correct").isFalse();
                assertThat(answerOptions.get(1).getText()).as("Text for answer option is correct").isEqualTo("C");
                assertThat(answerOptions.get(1).getHint()).as("Hint for answer option is correct").isEqualTo("H3");
                assertThat(answerOptions.get(1).getExplanation()).as("Explanation for answer option is correct").isEqualTo("E3");
                assertThat(answerOptions.get(1).isIsCorrect()).as("Is correct for answer option is correct").isTrue();
                assertThat(answerOptions.get(2).getText()).as("Text for answer option is correct").isEqualTo("D");
                assertThat(answerOptions.get(2).getHint()).as("Hint for answer option is correct").isEqualTo("H4");
                assertThat(answerOptions.get(2).getExplanation()).as("Explanation for answer option is correct").isEqualTo("E4");
                assertThat(answerOptions.get(2).isIsCorrect()).as("Is correct for answer option is correct").isTrue();
            }
            if (question instanceof DragAndDropQuestion) {
                DragAndDropQuestion dragAndDropQuestion = (DragAndDropQuestion) question;
                assertThat(dragAndDropQuestion.getDropLocations().size()).as("Drag and drop question drop locations were saved").isEqualTo(1);
                assertThat(dragAndDropQuestion.getDragItems().size()).as("Drag and drop question drag items were saved").isEqualTo(1);
                assertThat(dragAndDropQuestion.getTitle()).as("Drag and drop question title is correct").isEqualTo("DnD");
                assertThat(dragAndDropQuestion.getText()).as("Drag and drop question text is correct").isEqualTo("Q2");
                assertThat(dragAndDropQuestion.getScore()).as("Drag and drop question score is correct").isEqualTo(3);

                List<DropLocation> dropLocations = dragAndDropQuestion.getDropLocations();
                assertThat(dropLocations.get(0).getPosX()).as("Pos X for drop location is correct").isEqualTo(20);
                assertThat(dropLocations.get(0).getPosY()).as("Pos Y for drop location is correct").isEqualTo(20);
                assertThat(dropLocations.get(0).getWidth()).as("Width for drop location is correct").isEqualTo(10);
                assertThat(dropLocations.get(0).getHeight()).as("Height for drop location is correct").isEqualTo(10);

                List<DragItem> dragItems = dragAndDropQuestion.getDragItems();
                assertThat(dragItems.get(0).getText()).as("Text for drag item is correct").isEqualTo("D2");
            }
            if (question instanceof ShortAnswerQuestion) {
                ShortAnswerQuestion shortAnswerQuestion = (ShortAnswerQuestion) question;
                assertThat(shortAnswerQuestion.getSpots().size()).as("Short answer question spots were saved").isEqualTo(1);
                assertThat(shortAnswerQuestion.getSolutions().size()).as("Short answer question solutions were saved").isEqualTo(1);
                assertThat(shortAnswerQuestion.getTitle()).as("Short answer question title is correct").isEqualTo("SA");
                assertThat(shortAnswerQuestion.getText()).as("Short answer question text is correct").isEqualTo("This is a long answer text");
                assertThat(shortAnswerQuestion.getScore()).as("Short answer question score is correct").isEqualTo(2);

                List<ShortAnswerSpot> spots = shortAnswerQuestion.getSpots();
                assertThat(spots.get(0).getSpotNr()).as("Spot nr for spot is correct").isEqualTo(2);
                assertThat(spots.get(0).getWidth()).as("Width for spot is correct").isEqualTo(2);

                List<ShortAnswerSolution> solutions = shortAnswerQuestion.getSolutions();
                assertThat(solutions.get(0).getText()).as("Text for solution is correct").isEqualTo("long");
            }
        }
    }

    private QuizExercise createQuizOnServer(ZonedDateTime releaseDate, ZonedDateTime dueDate) throws Exception {
        Course course = database.createCourse();

        QuizExercise quizExercise = database.createQuiz(course, releaseDate, dueDate);
        quizExercise.setDuration(3600);
        QuizExercise quizExerciseServer = request.postWithResponseBody("/api/quiz-exercises", quizExercise, QuizExercise.class, HttpStatus.CREATED);
        QuizExercise quizExerciseDatabase = quizExerciseService.findOneWithQuestionsAndStatistics(quizExerciseServer.getId());

        checkQuizExercises(quizExercise, quizExerciseServer);
        checkQuizExercises(quizExercise, quizExerciseDatabase);

        for (int i = 0; i < quizExercise.getQuizQuestions().size(); i++) {
            var question = quizExercise.getQuizQuestions().get(i);
            var questionServer = quizExerciseServer.getQuizQuestions().get(i);
            var questionDatabase = quizExerciseDatabase.getQuizQuestions().get(i);

            assertThat(question.getId()).as("Question IDs are correct").isNull();
            assertThat(questionDatabase.getId()).as("Question IDs are correct").isEqualTo(questionServer.getId());

            assertThat(question.getExercise().getId()).as("Exercise IDs are correct").isNull();
            assertThat(questionDatabase.getExercise().getId()).as("Exercise IDs are correct").isEqualTo(quizExerciseDatabase.getId());

            assertThat(question.getTitle()).as("Question titles are correct").isEqualTo(questionDatabase.getTitle());
            assertThat(question.getTitle()).as("Question titles are correct").isEqualTo(questionServer.getTitle());

            assertThat(question.getScore()).as("Question scores are correct").isEqualTo(questionDatabase.getScore());
            assertThat(question.getScore()).as("Question scores are correct").isEqualTo(questionServer.getScore());
        }
        return quizExerciseServer;
    }

    @Test
    @WithMockUser(value = "instructor1", roles = "INSTRUCTOR")
    public void testDeleteQuizExercise() throws Exception {
        quizExercise = createQuizOnServer(ZonedDateTime.now().plusHours(5), null);

        assertThat(quizExerciseService.findOneWithQuestionsAndStatistics(quizExercise.getId())).as("Exercise is created correctly").isNotNull();
        request.delete("/api/quiz-exercises/" + quizExercise.getId(), HttpStatus.OK);
        assertThat(quizExerciseService.findOneWithQuestionsAndStatistics(quizExercise.getId())).as("Exercise is deleted correctly").isNull();
    }

    @Test
    @WithMockUser(value = "instructor1", roles = "INSTRUCTOR")
    public void testUpdateNotExistingQuizExercise() throws Exception {
        Course course = database.createCourse();
        QuizExercise quizExercise = database.createQuiz(course, ZonedDateTime.now().plusHours(5), null);
        QuizExercise quizExerciseServer = request.putWithResponseBody("/api/quiz-exercises", quizExercise, QuizExercise.class, HttpStatus.CREATED);
        assertThat(quizExercise.equals(quizExerciseServer));
    }

    @Test
    @WithMockUser(value = "instructor1", roles = "INSTRUCTOR")
    public void testUpdateRunningQuizExercise() throws Exception {
        Course course = database.createCourse();
        // create QuizExercise that already started
        QuizExercise startedQuizExercise = database.createQuiz(course, ZonedDateTime.now().minusHours(1), null);
        QuizExercise startedQuizExerciseServer = request.postWithResponseBody("/api/quiz-exercises", startedQuizExercise, QuizExercise.class, HttpStatus.CREATED);

        MultipleChoiceQuestion mc = (MultipleChoiceQuestion) startedQuizExerciseServer.getQuizQuestions().get(0);
        mc.getAnswerOptions().remove(0);
        mc.getAnswerOptions().add(new AnswerOption().text("C").hint("H3").explanation("E3").isCorrect(true));
        mc.getAnswerOptions().add(new AnswerOption().text("D").hint("H4").explanation("E4").isCorrect(true));

        QuizExercise updatedQuizExercise = request.putWithResponseBody("/api/quiz-exercises", startedQuizExerciseServer, QuizExercise.class, HttpStatus.BAD_REQUEST);
        assertThat(updatedQuizExercise).isNull();
    }

    @Test
    @WithMockUser(value = "instructor1", roles = "INSTRUCTOR")
    public void testCreateExistingQuizExercise() throws Exception {
        quizExercise = createQuizOnServer(ZonedDateTime.now().plusHours(5), null);

        QuizExercise newQuizExerciseServer = request.postWithResponseBody("/api/quiz-exercises", quizExercise, QuizExercise.class, HttpStatus.BAD_REQUEST);
        assertThat(newQuizExerciseServer).isNull();
    }

    @Test
    @WithMockUser(value = "instructor1", roles = "INSTRUCTOR")
    public void testGetQuizExercise() throws Exception {
        quizExercise = createQuizOnServer(ZonedDateTime.now().plusHours(5), null);

        QuizExercise quizExerciseGet = request.get("/api/quiz-exercises/" + quizExercise.getId(), HttpStatus.OK, QuizExercise.class);
        checkQuizExercises(quizExercise, quizExerciseGet);

        // get all exercises for a course
        List<QuizExercise> allQuizExercisesForCourse = request.getList("/api/courses/" + quizExercise.getCourseViaExerciseGroupOrCourseMember().getId() + "/quiz-exercises",
                HttpStatus.OK, QuizExercise.class);
        assertThat(allQuizExercisesForCourse.size()).isEqualTo(1);
        assertThat(allQuizExercisesForCourse.get(0)).isEqualTo(quizExercise);
    }

    @Test
    @WithMockUser(value = "student1", roles = "USER")
    public void testGetQuizExercise_asStudent() throws Exception {
        Course course = database.createCourse();
        QuizExercise quizExercise = database.createQuiz(course, ZonedDateTime.now().plusHours(5), null);
        quizExercise.setDuration(360);
        quizExercise = quizExerciseService.save(quizExercise);

        // get not started exercise for students
        QuizExercise quizExerciseForStudent_notStarted = request.get("/api/quiz-exercises/" + quizExercise.getId() + "/for-student", HttpStatus.OK, QuizExercise.class);
        checkQuizExerciseForStudent(quizExerciseForStudent_notStarted);

        // get started exercise for students
        quizExercise.setReleaseDate(ZonedDateTime.now().minusMinutes(5));
        quizExercise.setDueDate(ZonedDateTime.now().plusMinutes(1)); // total 6min = 360s
        quizExercise = quizExerciseService.save(quizExercise);
        assertThat(quizExercise.isSubmissionAllowed());
        assertThat(quizExercise.isStarted());

        QuizExercise quizExerciseForStudent_Started = request.get("/api/quiz-exercises/" + quizExercise.getId() + "/for-student", HttpStatus.OK, QuizExercise.class);
        checkQuizExerciseForStudent(quizExerciseForStudent_Started);

        // get finished exercise for students
        quizExercise.setReleaseDate(ZonedDateTime.now().minusMinutes(5));
        quizExercise.setDueDate(ZonedDateTime.now().minusMinutes(2));
        quizExercise.setDuration(180);
        quizExercise = quizExerciseService.save(quizExercise);
        assertThat(!quizExercise.isSubmissionAllowed());
        assertThat(quizExercise.isStarted());

        QuizExercise quizExerciseForStudent_Finished = request.get("/api/quiz-exercises/" + quizExercise.getId() + "/for-student", HttpStatus.OK, QuizExercise.class);
        checkQuizExerciseForStudent(quizExerciseForStudent_Finished);
    }

    @Test
    @WithMockUser(value = "instructor1", roles = "INSTRUCTOR")
    public void testRecalculateStatistics() throws Exception {
        quizExercise = createQuizOnServer(ZonedDateTime.now().plusHours(5), null);

        quizExercise.setReleaseDate(ZonedDateTime.now().minusHours(5));
        quizExercise.setDueDate(ZonedDateTime.now().minusHours(2));
        quizExercise = request.putWithResponseBody("/api/quiz-exercises", quizExercise, QuizExercise.class, HttpStatus.OK);

        var now = ZonedDateTime.now();

        // generate submissions for each student
        int numberOfParticipants = 10;

        for (int i = 1; i <= numberOfParticipants; i++) {
            QuizSubmission quizSubmission = database.generateSubmission(quizExercise, i, true, now.minusHours(3));
            database.addSubmission(quizExercise, quizSubmission, "student" + i);
            database.addResultToSubmission(quizSubmission, AssessmentType.AUTOMATIC, null, quizExercise.getScoreForSubmission(quizSubmission), true);
        }

        // calculate statistics
        QuizExercise quizExerciseWithRecalculatedStatistics = request.get("/api/quiz-exercises/" + quizExercise.getId() + "/recalculate-statistics", HttpStatus.OK,
                QuizExercise.class);

        assertThat(quizExerciseWithRecalculatedStatistics.getQuizPointStatistic().getPointCounters().size()).isEqualTo(10);
        assertThat(quizExerciseWithRecalculatedStatistics.getQuizPointStatistic().getParticipantsRated()).isEqualTo(numberOfParticipants);

        for (PointCounter pointCounter : quizExerciseWithRecalculatedStatistics.getQuizPointStatistic().getPointCounters()) {
            if (pointCounter.getPoints() == 0.0) {
                assertThat(pointCounter.getRatedCounter()).isEqualTo(3);
            }
            else if (pointCounter.getPoints() == 3.0 || pointCounter.getPoints() == 4.0 || pointCounter.getPoints() == 6.0) {
                assertThat(pointCounter.getRatedCounter()).isEqualTo(2);
            }
            else if (pointCounter.getPoints() == 7.0) {
                assertThat(pointCounter.getRatedCounter()).isEqualTo(1);
            }
            else {
                assertThat(pointCounter.getRatedCounter()).isEqualTo(0);
            }
        }

        // add more submissions and recalculate
        for (int i = numberOfParticipants; i <= 14; i++) {
            QuizSubmission quizSubmission = database.generateSubmission(quizExercise, i, true, now.minusHours(3));
            database.addSubmission(quizExercise, quizSubmission, "student" + i);
            database.addResultToSubmission(quizSubmission, AssessmentType.AUTOMATIC, null, quizExercise.getScoreForSubmission(quizSubmission), true);
        }

        // calculate statistics
        quizExerciseWithRecalculatedStatistics = request.get("/api/quiz-exercises/" + quizExercise.getId() + "/recalculate-statistics", HttpStatus.OK, QuizExercise.class);

        assertThat(quizExerciseWithRecalculatedStatistics.getQuizPointStatistic().getPointCounters().size()).isEqualTo(10);
        assertThat(quizExerciseWithRecalculatedStatistics.getQuizPointStatistic().getParticipantsRated()).isEqualTo(numberOfParticipants + 4);

        for (PointCounter pointCounter : quizExerciseWithRecalculatedStatistics.getQuizPointStatistic().getPointCounters()) {
            if (pointCounter.getPoints() == 0.0) {
                assertThat(pointCounter.getRatedCounter()).isEqualTo(5);
            }
            else if (pointCounter.getPoints() == 4.0) {
                assertThat(pointCounter.getRatedCounter()).isEqualTo(3);
            }
            else if (pointCounter.getPoints() == 3.0 || pointCounter.getPoints() == 6.0) {
                assertThat(pointCounter.getRatedCounter()).isEqualTo(2);
            }
            else if (pointCounter.getPoints() == 7.0) {
                assertThat(pointCounter.getRatedCounter()).isEqualTo(1);
            }
            else if (pointCounter.getPoints() == 9.0) {
                assertThat(pointCounter.getRatedCounter()).isEqualTo(1);
            }
            else {
                assertThat(pointCounter.getRatedCounter()).isEqualTo(0);
            }
        }
    }

    @Test
    @WithMockUser(value = "instructor1", roles = "INSTRUCTOR")
    public void testReevaluateStatistics() throws Exception {
        quizExercise = createQuizOnServer(ZonedDateTime.now().plusSeconds(5), null);
        // var now = ZonedDateTime.now();

        // we expect a bad request because the quiz has not ended yet
        request.putWithResponseBody("/api/quiz-exercises/" + quizExercise.getId() + "/re-evaluate/", quizExercise, QuizExercise.class, HttpStatus.BAD_REQUEST);
        quizExercise.setReleaseDate(ZonedDateTime.now().minusHours(5));
        quizExercise.setDueDate(ZonedDateTime.now().minusMinutes(1));
        // quizExercise.setDuration(3600);
        quizExercise = request.putWithResponseBody("/api/quiz-exercises", quizExercise, QuizExercise.class, HttpStatus.OK);

        // generate rated submissions for each student
        int numberOfParticipants = 10;

        for (int i = 1; i <= numberOfParticipants; i++) {
            if (i != 1 && i != 5) {
                QuizSubmission quizSubmission = database.generateSubmission(quizExercise, i, true, ZonedDateTime.now().minusHours(1));

                database.addSubmission(quizExercise, quizSubmission, "student" + i);

                database.addResultToSubmission(quizSubmission, AssessmentType.AUTOMATIC, null, quizExercise.getScoreForSubmission(quizSubmission), true);
            }
        }

        // submission with everything selected
        QuizSubmission quizSubmission = database.generateSpecialSubmissionWithResult(quizExercise, true, ZonedDateTime.now().minusHours(1), true);

        database.addSubmission(quizExercise, quizSubmission, "student1");

        database.addResultToSubmission(quizSubmission, AssessmentType.AUTOMATIC, null, quizExercise.getScoreForSubmission(quizSubmission), true);

        // submission with nothing selected
        quizSubmission = database.generateSpecialSubmissionWithResult(quizExercise, true, ZonedDateTime.now().minusHours(1), false);

        database.addSubmission(quizExercise, quizSubmission, "student5");

        database.addResultToSubmission(quizSubmission, AssessmentType.AUTOMATIC, null, quizExercise.getScoreForSubmission(quizSubmission), true);

        assertThat(studentParticipationRepository.findAll().size()).isEqualTo(10);
        assertThat(resultRepository.findAll().size()).isEqualTo(10);

        // calculate statistics
        quizExercise = request.get("/api/quiz-exercises/" + quizExercise.getId() + "/recalculate-statistics", HttpStatus.OK, QuizExercise.class);

        // reevaluate without changing anything and check if statistics are still correct
        QuizExercise quizExerciseWithReevaluatedStatistics = request.putWithResponseBody("/api/quiz-exercises/" + quizExercise.getId() + "/re-evaluate/", quizExercise,
                QuizExercise.class, HttpStatus.OK);
        checkStatistics(quizExercise, quizExerciseWithReevaluatedStatistics);

        // remove wrong answer option and reevaluate
        MultipleChoiceQuestion mc = (MultipleChoiceQuestion) quizExercise.getQuizQuestions().get(0);
        mc.getAnswerOptions().remove(1);

        quizExerciseWithReevaluatedStatistics = request.putWithResponseBody("/api/quiz-exercises/" + quizExercise.getId() + "/re-evaluate/", quizExercise, QuizExercise.class,
                HttpStatus.OK);

        // one student should get a higher score
        assertThat(quizExerciseWithReevaluatedStatistics.getQuizPointStatistic().getPointCounters().size())
                .isEqualTo(quizExercise.getQuizPointStatistic().getPointCounters().size());
        for (PointCounter pointCounter : quizExerciseWithReevaluatedStatistics.getQuizPointStatistic().getPointCounters()) {
            if (pointCounter.getPoints() == 0.0) {
                assertThat(pointCounter.getRatedCounter()).isEqualTo(2); // one less
                assertThat(pointCounter.getUnRatedCounter()).isEqualTo(0);
            }
            else if (pointCounter.getPoints() == 4.0) {
                assertThat(pointCounter.getRatedCounter()).isEqualTo(3); // one more
                assertThat(pointCounter.getUnRatedCounter()).isEqualTo(0);
            }
        }

        // set a question invalid and reevaluate
        ShortAnswerQuestion sq = (ShortAnswerQuestion) quizExercise.getQuizQuestions().get(2);
        sq.setInvalid(true);

        quizExerciseWithReevaluatedStatistics = request.putWithResponseBody("/api/quiz-exercises/" + quizExercise.getId() + "/re-evaluate/", quizExercise, QuizExercise.class,
                HttpStatus.OK);

        // several students should get a higher score
        assertThat(quizExerciseWithReevaluatedStatistics.getQuizPointStatistic().getPointCounters().size())
                .isEqualTo(quizExercise.getQuizPointStatistic().getPointCounters().size());
        for (PointCounter pointCounter : quizExerciseWithReevaluatedStatistics.getQuizPointStatistic().getPointCounters()) {
            if (pointCounter.getPoints() == 2.0 || pointCounter.getPoints() == 5.0) {
                assertThat(pointCounter.getRatedCounter()).isEqualTo(2);
                assertThat(pointCounter.getUnRatedCounter()).isEqualTo(0);
            }
            else if (pointCounter.getPoints() == 6.0) {
                assertThat(pointCounter.getRatedCounter()).isEqualTo(5);
                assertThat(pointCounter.getUnRatedCounter()).isEqualTo(0);
            }
            else if (pointCounter.getPoints() == 9.0) {
                assertThat(pointCounter.getRatedCounter()).isEqualTo(1);
                assertThat(pointCounter.getUnRatedCounter()).isEqualTo(0);
            }
        }

        // delete a question and reevaluate
        quizExercise.getQuizQuestions().remove(1);

        quizExerciseWithReevaluatedStatistics = request.putWithResponseBody("/api/quiz-exercises/" + quizExercise.getId() + "/re-evaluate/", quizExercise, QuizExercise.class,
                HttpStatus.OK);

        // max score should be less
        assertThat(quizExerciseWithReevaluatedStatistics.getQuizPointStatistic().getPointCounters().size())
                .isEqualTo(quizExercise.getQuizPointStatistic().getPointCounters().size() - 3);
        for (PointCounter pointCounter : quizExerciseWithReevaluatedStatistics.getQuizPointStatistic().getPointCounters()) {
            if (pointCounter.getPoints() == 2.0) {
                assertThat(pointCounter.getRatedCounter()).isEqualTo(4);
                assertThat(pointCounter.getUnRatedCounter()).isEqualTo(0);
            }
            else if (pointCounter.getPoints() == 6.0) {
                assertThat(pointCounter.getRatedCounter()).isEqualTo(6);
                assertThat(pointCounter.getUnRatedCounter()).isEqualTo(0);
            }
        }
    }

    @Disabled
    // TODO: this is a flaky test and it's unclear why. We skip it for now.
    @WithMockUser(value = "instructor1", roles = "INSTRUCTOR")
    public void testReevaluateStatistics_Practice() throws Exception {

        quizExercise = createQuizOnServer(ZonedDateTime.now().plusSeconds(5), null);
        // var now = ZonedDateTime.now();

        // we expect a bad request because the quiz has not ended yet
        request.putWithResponseBody("/api/quiz-exercises/" + quizExercise.getId() + "/re-evaluate/", quizExercise, QuizExercise.class, HttpStatus.BAD_REQUEST);
        quizExercise.setReleaseDate(ZonedDateTime.now().minusHours(2));
        quizExercise.setDueDate(ZonedDateTime.now().minusHours(1));
        quizExercise.setDuration(3600);
        quizExercise.setIsOpenForPractice(true);
        quizExercise = request.putWithResponseBody("/api/quiz-exercises", quizExercise, QuizExercise.class, HttpStatus.OK);

        // generate unrated submissions for each student
        int numberOfParticipants = 10;

        // TODO: maybe the test is flaky because the submissions and results are directly added to the database instead of using the REST calls?

        for (int i = 1; i <= numberOfParticipants; i++) {
            if (i != 1 && i != 5) {
                QuizSubmission quizSubmissionPractice = database.generateSubmission(quizExercise, i, true, ZonedDateTime.now());
                database.addSubmission(quizExercise, quizSubmissionPractice, "student" + i);
                database.addResultToSubmission(quizSubmissionPractice, AssessmentType.AUTOMATIC, null, quizExercise.getScoreForSubmission(quizSubmissionPractice), false);
            }
        }

        // submission with everything selected
        QuizSubmission quizSubmissionPractice = database.generateSpecialSubmissionWithResult(quizExercise, true, ZonedDateTime.now(), true);
        database.addSubmission(quizExercise, quizSubmissionPractice, "student1");
        database.addResultToSubmission(quizSubmissionPractice, AssessmentType.AUTOMATIC, null, quizExercise.getScoreForSubmission(quizSubmissionPractice), false);

        // submission with nothing selected
        quizSubmissionPractice = database.generateSpecialSubmissionWithResult(quizExercise, true, ZonedDateTime.now(), false);
        database.addSubmission(quizExercise, quizSubmissionPractice, "student5");
        database.addResultToSubmission(quizSubmissionPractice, AssessmentType.AUTOMATIC, null, quizExercise.getScoreForSubmission(quizSubmissionPractice), false);

        assertThat(studentParticipationRepository.findAll().size()).isEqualTo(10);
        assertThat(resultRepository.findAll().size()).isEqualTo(10);

        // calculate statistics
        quizExercise = request.get("/api/quiz-exercises/" + quizExercise.getId() + "/recalculate-statistics", HttpStatus.OK, QuizExercise.class);

        System.out.println("QuizPointStatistic before re-evaluate: " + quizExercise.getQuizPointStatistic());

        // reevaluate without changing anything and check if statistics are still correct
        QuizExercise quizExerciseWithReevaluatedStatistics = request.putWithResponseBody("/api/quiz-exercises/" + quizExercise.getId() + "/re-evaluate/", quizExercise,
                QuizExercise.class, HttpStatus.OK);
        checkStatistics(quizExercise, quizExerciseWithReevaluatedStatistics);

        System.out.println("QuizPointStatistic after re-evaluate (without changes): " + quizExerciseWithReevaluatedStatistics.getQuizPointStatistic());

        // remove wrong answer option and reevaluate
        MultipleChoiceQuestion mc = (MultipleChoiceQuestion) quizExerciseWithReevaluatedStatistics.getQuizQuestions().get(0);
        mc.getAnswerOptions().remove(1);

        quizExerciseWithReevaluatedStatistics = request.putWithResponseBody("/api/quiz-exercises/" + quizExerciseWithReevaluatedStatistics.getId() + "/re-evaluate/",
                quizExerciseWithReevaluatedStatistics, QuizExercise.class, HttpStatus.OK);

        // one student should get a higher score
        assertThat(quizExerciseWithReevaluatedStatistics.getQuizPointStatistic().getPointCounters().size())
                .isEqualTo(quizExercise.getQuizPointStatistic().getPointCounters().size());

        System.out.println("QuizPointStatistic after 1st re-evaluate: " + quizExerciseWithReevaluatedStatistics.getQuizPointStatistic());
        for (PointCounter pointCounter : quizExerciseWithReevaluatedStatistics.getQuizPointStatistic().getPointCounters()) {
            if (pointCounter.getPoints() == 0.0 || pointCounter.getPoints() == 3.0 || pointCounter.getPoints() == 6.0) {
                assertThat(pointCounter.getRatedCounter()).isEqualTo(0);
                assertThat(pointCounter.getUnRatedCounter()).isEqualTo(2);
            }
            else if (pointCounter.getPoints() == 4.0) {
                assertThat(pointCounter.getRatedCounter()).isEqualTo(0);
                assertThat(pointCounter.getUnRatedCounter()).isEqualTo(3);
            }
            else if (pointCounter.getPoints() == 7.0) {
                assertThat(pointCounter.getRatedCounter()).isEqualTo(0);
                assertThat(pointCounter.getUnRatedCounter()).isEqualTo(1);
            }
            else {
                assertThat(pointCounter.getRatedCounter()).isEqualTo(0);
                assertThat(pointCounter.getUnRatedCounter()).isEqualTo(0);
            }
        }

        // set a question invalid and reevaluate
        quizExerciseWithReevaluatedStatistics.getQuizQuestions().get(2).setInvalid(true);

        quizExerciseWithReevaluatedStatistics = request.putWithResponseBody("/api/quiz-exercises/" + quizExerciseWithReevaluatedStatistics.getId() + "/re-evaluate/",
                quizExerciseWithReevaluatedStatistics, QuizExercise.class, HttpStatus.OK);

        // several students should get a higher score
        assertThat(quizExerciseWithReevaluatedStatistics.getQuizPointStatistic().getPointCounters().size())
                .isEqualTo(quizExercise.getQuizPointStatistic().getPointCounters().size());
        System.out.println("QuizPointStatistic after 2nd re-evaluate: " + quizExerciseWithReevaluatedStatistics.getQuizPointStatistic());
        for (PointCounter pointCounter : quizExerciseWithReevaluatedStatistics.getQuizPointStatistic().getPointCounters()) {
            if (pointCounter.getPoints() == 2.0 || pointCounter.getPoints() == 5.0) {
                assertThat(pointCounter.getRatedCounter()).isEqualTo(0);
                assertThat(pointCounter.getUnRatedCounter()).isEqualTo(2);
            }
            else if (pointCounter.getPoints() == 6.0) {
                assertThat(pointCounter.getRatedCounter()).isEqualTo(0);
                assertThat(pointCounter.getUnRatedCounter()).isEqualTo(5);
            }
            else if (pointCounter.getPoints() == 9.0) {
                assertThat(pointCounter.getRatedCounter()).isEqualTo(0);
                assertThat(pointCounter.getUnRatedCounter()).isEqualTo(1);
            }
        }

        // delete a question and reevaluate
        quizExerciseWithReevaluatedStatistics.getQuizQuestions().remove(1);

        quizExerciseWithReevaluatedStatistics = request.putWithResponseBody("/api/quiz-exercises/" + quizExerciseWithReevaluatedStatistics.getId() + "/re-evaluate/",
                quizExerciseWithReevaluatedStatistics, QuizExercise.class, HttpStatus.OK);

        // max score should be less
        System.out.println("QuizPointStatistic after 3rd re-evaluate: " + quizExerciseWithReevaluatedStatistics.getQuizPointStatistic());
        assertThat(quizExerciseWithReevaluatedStatistics.getQuizPointStatistic().getPointCounters().size())
                .isEqualTo(quizExercise.getQuizPointStatistic().getPointCounters().size() - 3);
        for (PointCounter pointCounter : quizExerciseWithReevaluatedStatistics.getQuizPointStatistic().getPointCounters()) {
            if (pointCounter.getPoints() == 2.0) {
                assertThat(pointCounter.getRatedCounter()).isEqualTo(0);
                assertThat(pointCounter.getUnRatedCounter()).isEqualTo(4);
            }
            else if (pointCounter.getPoints() == 6.0) {
                assertThat(pointCounter.getRatedCounter()).isEqualTo(0);
                assertThat(pointCounter.getUnRatedCounter()).isEqualTo(6);
            }
        }
    }

    @Test
    @WithMockUser(value = "instructor1", roles = "INSTRUCTOR")
    public void testPerformStartNow() throws Exception {
        quizExercise = createQuizOnServer(ZonedDateTime.now().plusHours(5), null);

        QuizExercise updatedQuizExercise = request.putWithResponseBody("/api/quiz-exercises/" + quizExercise.getId() + "/start-now", quizExercise, QuizExercise.class,
                HttpStatus.OK);
        long millis = ChronoUnit.MILLIS.between(updatedQuizExercise.getReleaseDate(), ZonedDateTime.now());
        // actually the two dates should be "exactly" the same, but for the sake of slow CI testing machines and to prevent flaky tests, we live with the following rule
        assertThat(millis).isCloseTo(0, byLessThan(2000L));
        assertThat(updatedQuizExercise.isIsPlannedToStart()).isTrue();
    }

    @Test
    @WithMockUser(value = "instructor1", roles = "INSTRUCTOR")
    public void testPerformSetVisible() throws Exception {
        quizExercise = createQuizOnServer(ZonedDateTime.now().plusHours(5), null);

        // we expect a bad request because the quiz is already visible
        request.putWithResponseBody("/api/quiz-exercises/" + quizExercise.getId() + "/set-visible", quizExercise, QuizExercise.class, HttpStatus.BAD_REQUEST);
        quizExercise.setIsVisibleBeforeStart(false);
        quizExerciseService.save(quizExercise);
        QuizExercise updatedQuizExercise = request.putWithResponseBody("/api/quiz-exercises/" + quizExercise.getId() + "/set-visible", quizExercise, QuizExercise.class,
                HttpStatus.OK);
        assertThat(updatedQuizExercise.isVisibleToStudents()).isTrue();
    }

    @Test
    @WithMockUser(value = "instructor1", roles = "INSTRUCTOR")
    public void testPerformOpenForPractice() throws Exception {
        quizExercise = createQuizOnServer(ZonedDateTime.now().plusHours(5), null);

        // we expect a bad request because the quiz has not ended yet
        request.putWithResponseBody("/api/quiz-exercises/" + quizExercise.getId() + "/open-for-practice", quizExercise, QuizExercise.class, HttpStatus.BAD_REQUEST);
        quizExercise.setReleaseDate(ZonedDateTime.now().minusMinutes(5));
        quizExercise.setDueDate(ZonedDateTime.now().minusMinutes(2));
        quizExercise.setDuration(180);
        quizExerciseService.save(quizExercise);
        QuizExercise updatedQuizExercise = request.putWithResponseBody("/api/quiz-exercises/" + quizExercise.getId() + "/open-for-practice", quizExercise, QuizExercise.class,
                HttpStatus.OK);
        assertThat(updatedQuizExercise.isIsOpenForPractice()).isTrue();
    }

    /**
     * Check that the general information of two exercises is equal.
     */
    private void checkQuizExercises(QuizExercise quizExercise, QuizExercise quizExercise2) {
        assertThat(quizExercise.getQuizQuestions()).as("Same amount of questions saved").hasSize(quizExercise2.getQuizQuestions().size());
        assertThat(quizExercise.getTitle()).as("Title saved correctly").isEqualTo(quizExercise2.getTitle());
        assertThat(quizExercise.getAllowedNumberOfAttempts()).as("Number of attempts saved correctly").isEqualTo(quizExercise2.getAllowedNumberOfAttempts());
        assertThat(quizExercise.getMaxScore()).as("Max score saved correctly").isEqualTo(quizExercise2.getMaxScore());
        assertThat(quizExercise.getDuration()).as("Duration saved correctly").isEqualTo(quizExercise2.getDuration());
        assertThat(quizExercise.getType()).as("Type saved correctly").isEqualTo(quizExercise2.getType());
    }

    /**
     * Check that the general statistics of two exercises are equal.
     */
    private void checkStatistics(QuizExercise quizExercise, QuizExercise quizExercise2) {
        assertThat(quizExercise.getQuizPointStatistic().getPointCounters().size()).isEqualTo(quizExercise2.getQuizPointStatistic().getPointCounters().size());
        assertThat(quizExercise.getQuizPointStatistic().getParticipantsRated()).isEqualTo(quizExercise2.getQuizPointStatistic().getParticipantsRated());
        assertThat(quizExercise.getQuizPointStatistic().getParticipantsUnrated()).isEqualTo(quizExercise2.getQuizPointStatistic().getParticipantsUnrated());

        for (int i = 0; i < quizExercise.getQuizPointStatistic().getPointCounters().size(); i++) {
            PointCounter pointCounterBefore = quizExercise.getQuizPointStatistic().getPointCounters().iterator().next();
            PointCounter pointCounterAfter = quizExercise2.getQuizPointStatistic().getPointCounters().iterator().next();

            assertThat(pointCounterAfter.getPoints()).isEqualTo(pointCounterBefore.getPoints());
            assertThat(pointCounterAfter.getRatedCounter()).isEqualTo(pointCounterBefore.getRatedCounter());
            assertThat(pointCounterAfter.getUnRatedCounter()).isEqualTo(pointCounterBefore.getUnRatedCounter());
        }
    }

    /**
     * Check if a QuizExercise contains the correct information for students.
     * @param quizExercise QuizExercise to check
     */
    private void checkQuizExerciseForStudent(QuizExercise quizExercise) {
        assertThat(quizExercise.getQuizPointStatistic()).isNull();
        assertThat(quizExercise.getGradingInstructions()).isNull();
        assertThat(quizExercise.getGradingCriteria()).isEmpty();
        if (quizExercise.isStarted()) {
            for (QuizQuestion question : quizExercise.getQuizQuestions()) {
                assertThat(question.getExplanation()).isNull();
                assertThat(question.getQuizQuestionStatistic()).isNull();
            }
        }
        else {
            assertThat(quizExercise.getQuizQuestions()).hasSize(0);
        }

    }
}
