package de.tum.in.www1.artemis.util;

import static com.google.gson.JsonParser.parseString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.net.URL;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.TestSecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;

import de.tum.in.www1.artemis.domain.*;
import de.tum.in.www1.artemis.domain.enumeration.*;
import de.tum.in.www1.artemis.domain.exam.Exam;
import de.tum.in.www1.artemis.domain.exam.ExerciseGroup;
import de.tum.in.www1.artemis.domain.exam.StudentExam;
import de.tum.in.www1.artemis.domain.modeling.ModelingExercise;
import de.tum.in.www1.artemis.domain.modeling.ModelingSubmission;
import de.tum.in.www1.artemis.domain.participation.*;
import de.tum.in.www1.artemis.domain.quiz.*;
import de.tum.in.www1.artemis.repository.*;
import de.tum.in.www1.artemis.security.AuthoritiesConstants;
import de.tum.in.www1.artemis.service.ModelingAssessmentService;
import de.tum.in.www1.artemis.service.ModelingSubmissionService;

/** Service responsible for initializing the database with specific testdata for a testscenario */
@Service
public class DatabaseUtilService {

    private static ZonedDateTime pastTimestamp = ZonedDateTime.now().minusDays(1);

    private static ZonedDateTime futureTimestamp = ZonedDateTime.now().plusDays(1);

    private static ZonedDateTime futureFutureTimestamp = ZonedDateTime.now().plusDays(2);

    private static Authority userAuthority = new Authority(AuthoritiesConstants.USER);

    private static Authority tutorAuthority = new Authority(AuthoritiesConstants.TEACHING_ASSISTANT);

    private static Authority instructorAuthority = new Authority(AuthoritiesConstants.INSTRUCTOR);

    private static Authority adminAuthority = new Authority(AuthoritiesConstants.ADMIN);

    private static Set<Authority> studentAuthorities = Set.of(userAuthority);

    private static Set<Authority> tutorAuthorities = Set.of(userAuthority, tutorAuthority);

    private static Set<Authority> instructorAuthorities = Set.of(userAuthority, tutorAuthority, instructorAuthority);

    private static Set<Authority> adminAuthorities = Set.of(userAuthority, tutorAuthority, instructorAuthority, adminAuthority);

    @Autowired
    CourseRepository courseRepo;

    @Autowired
    LectureRepository lectureRepo;

    @Autowired
    ExerciseRepository exerciseRepo;

    @Autowired
    AttachmentRepository attachmentRepo;

    @Autowired
    ProgrammingExerciseTestCaseRepository testCaseRepository;

    @Autowired
    ProgrammingExerciseRepository programmingExerciseRepository;

    @Autowired
    ExerciseHintRepository exerciseHintRepository;

    @Autowired
    UserRepository userRepo;

    @Autowired
    TeamRepository teamRepo;

    @Autowired
    ResultRepository resultRepo;

    @Autowired
    StudentParticipationRepository studentParticipationRepo;

    @Autowired
    ProgrammingExerciseStudentParticipationRepository programmingExerciseStudentParticipationRepo;

    @Autowired
    TemplateProgrammingExerciseParticipationRepository templateProgrammingExerciseParticipationRepo;

    @Autowired
    SolutionProgrammingExerciseParticipationRepository solutionProgrammingExerciseParticipationRepo;

    @Autowired
    ModelingSubmissionRepository modelingSubmissionRepo;

    @Autowired
    TextSubmissionRepository textSubmissionRepo;

    @Autowired
    TextBlockRepository textBlockRepo;

    @Autowired
    FileUploadSubmissionRepository fileUploadSubmissionRepo;

    @Autowired
    SubmissionRepository submissionRepository;

    @Autowired
    SubmissionVersionRepository submissionVersionRepository;

    @Autowired
    ProgrammingSubmissionRepository programmingSubmissionRepo;

    @Autowired
    ModelAssessmentConflictRepository conflictRepo;

    @Autowired
    ConflictingResultRepository conflictingResultRepo;

    @Autowired
    FeedbackRepository feedbackRepo;

    @Autowired
    ComplaintRepository complaintRepo;

    @Autowired
    ComplaintResponseRepository complaintResponseRepo;

    @Autowired
    ExampleSubmissionRepository exampleSubmissionRepo;

    @Autowired
    TutorParticipationRepository tutorParticipationRepo;

    @Autowired
    StudentQuestionRepository studentQuestionRepository;

    @Autowired
    ModelingSubmissionService modelSubmissionService;

    @Autowired
    ModelingAssessmentService modelingAssessmentService;

    @Autowired
    ProgrammingExerciseTestRepository programmingExerciseTestRepository;

    @Autowired
    private LtiUserIdRepository ltiUserIdRepository;

    @Autowired
    private LtiOutcomeUrlRepository ltiOutcomeUrlRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    @Autowired
    ObjectMapper mapper;

    @Autowired
    GroupNotificationRepository groupNotificationRepository;

    @Autowired
    private TextClusterRepository textClusterRepository;

    @Autowired
    private ExerciseGroupRepository exerciseGroupRepository;

    @Autowired
    private StudentExamRepository studentExamRepository;

    @Autowired
    private ExamRepository examRepository;

    public void resetDatabase() {

        conflictRepo.deleteAll();
        conflictingResultRepo.deleteAll();
        complaintResponseRepo.deleteAll();
        complaintRepo.deleteAll();
        resultRepo.deleteAll();
        assertThat(resultRepo.findAll()).as("result data has been cleared").isEmpty();
        feedbackRepo.deleteAll();
        tutorParticipationRepo.deleteAll();
        exampleSubmissionRepo.deleteAll();
        modelingSubmissionRepo.deleteAll();
        textSubmissionRepo.deleteAll();
        textClusterRepository.deleteAll();
        fileUploadSubmissionRepo.deleteAll();
        programmingSubmissionRepo.deleteAll();
        submissionRepository.deleteAll();
        submissionVersionRepository.deleteAll();
        studentQuestionRepository.deleteAll();
        studentParticipationRepo.deleteAll();
        assertThat(studentParticipationRepo.findAll()).as("participation data has been cleared").isEmpty();
        teamRepo.deleteAll();
        ltiOutcomeUrlRepository.deleteAll();
        programmingExerciseRepository.deleteAll();
        groupNotificationRepository.deleteAll();
        studentExamRepository.deleteAll();
        exerciseRepo.deleteAll();
        assertThat(exerciseRepo.findAll()).as("exercise data has been cleared").isEmpty();
        examRepository.deleteAll();
        assertThat(examRepository.findAll()).as("result data has been cleared").isEmpty();
        exerciseGroupRepository.deleteAll();
        attachmentRepo.deleteAll();
        lectureRepo.deleteAll();
        courseRepo.deleteAll();

        assertThat(resultRepo.findAll()).as("result data has been cleared").isEmpty();
        assertThat(courseRepo.findAll()).as("course data has been cleared").isEmpty();
        ltiUserIdRepository.deleteAll();
        userRepo.deleteAll();
        authorityRepository.deleteAll();
        assertThat(userRepo.findAll()).as("user data has been cleared").isEmpty();
        assertThat(testCaseRepository.findAll()).as("test case data has been cleared").isEmpty();
    }

    // TODO: this should probably be moved into another service
    public void changeUser(String username) {
        User user = getUserByLogin(username);
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (Authority authority : user.getAuthorities()) {
            grantedAuthorities.add(new SimpleGrantedAuthority(authority.getName()));
        }
        org.springframework.security.core.userdetails.User securityContextUser = new org.springframework.security.core.userdetails.User(user.getLogin(), user.getPassword(),
                grantedAuthorities);
        Authentication authentication = new UsernamePasswordAuthenticationToken(securityContextUser, securityContextUser.getPassword(), grantedAuthorities);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        TestSecurityContextHolder.setContext(context);
    }

    /**
     * Adds the provided number of students and tutors into the user repository. Students login is a concatenation of the prefix "student" and a number counting from 1 to
     * numberOfStudents Tutors login is a concatenation of the prefix "tutor" and a number counting from 1 to numberOfStudents Tutors are all in the "tutor" group and students in
     * the "tumuser" group
     *
     * @param numberOfStudents the number of students that will be added to the database
     * @param numberOfTutors the number of tutors that will be added to the database
     * @param numberOfInstructors the number of instructors that will be added to the database
     */
    public List<User> addUsers(int numberOfStudents, int numberOfTutors, int numberOfInstructors) {

        authorityRepository.saveAll(adminAuthorities);

        List<User> students = ModelFactory.generateActivatedUsers("student", new String[] { "tumuser", "testgroup" }, studentAuthorities, numberOfStudents);
        List<User> tutors = ModelFactory.generateActivatedUsers("tutor", new String[] { "tutor", "testgroup" }, tutorAuthorities, numberOfTutors);
        List<User> instructors = ModelFactory.generateActivatedUsers("instructor", new String[] { "instructor", "testgroup" }, instructorAuthorities, numberOfInstructors);
        User admin = ModelFactory.generateActivatedUser("admin");
        admin.setGroups(Set.of("admin"));
        List<User> usersToAdd = new ArrayList<>();
        usersToAdd.addAll(students);
        usersToAdd.addAll(tutors);
        usersToAdd.addAll(instructors);
        usersToAdd.add(admin);
        userRepo.saveAll(usersToAdd);
        assertThat(userRepo.findAll().size()).as("all users are created").isGreaterThanOrEqualTo(numberOfStudents + numberOfTutors + numberOfInstructors + 1);
        assertThat(userRepo.findAll()).as("users are correctly stored").containsAnyOf(usersToAdd.toArray(new User[0]));

        final var users = new ArrayList<>(students);
        users.addAll(tutors);
        users.addAll(instructors);
        users.add(admin);
        return users;
    }

    public List<Team> addTeamsForExercise(Exercise exercise, String shortNamePrefix, String loginPrefix, int numberOfTeams, User owner) {
        List<Team> teams = ModelFactory.generateTeamsForExercise(exercise, shortNamePrefix, loginPrefix, numberOfTeams, owner);
        userRepo.saveAll(teams.stream().map(Team::getStudents).flatMap(Collection::stream).collect(Collectors.toList()));
        return teamRepo.saveAll(teams);
    }

    public List<Team> addTeamsForExercise(Exercise exercise, String shortNamePrefix, int numberOfTeams, User owner) {
        return addTeamsForExercise(exercise, shortNamePrefix, "student", numberOfTeams, owner);
    }

    public List<Team> addTeamsForExercise(Exercise exercise, int numberOfTeams, User owner) {
        return addTeamsForExercise(exercise, "team", numberOfTeams, owner);
    }

    public Team addTeamForExercise(Exercise exercise, User owner) {
        return addTeamsForExercise(exercise, 1, owner).get(0);
    }

    public Result addProgrammingParticipationWithResultForExercise(ProgrammingExercise exercise, String login) {
        var storedParticipation = programmingExerciseStudentParticipationRepo.findByExerciseIdAndStudentLogin(exercise.getId(), login);
        final StudentParticipation studentParticipation;
        if (storedParticipation.isEmpty()) {
            final var user = getUserByLogin(login);
            final var participation = new ProgrammingExerciseStudentParticipation();
            final var buildPlanId = exercise.getProjectKey().toUpperCase() + "-" + login.toUpperCase();
            final var repoName = (exercise.getProjectKey() + "-" + login).toLowerCase();
            participation.setInitializationDate(ZonedDateTime.now());
            participation.setParticipant(user);
            participation.setBuildPlanId(buildPlanId);
            participation.setProgrammingExercise(exercise);
            participation.setInitializationState(InitializationState.INITIALIZED);
            participation.setRepositoryUrl(String.format("http://some.test.url/%s/%s.git", exercise.getCourseViaExerciseGroupOrCourseMember().getShortName(), repoName));
            programmingExerciseStudentParticipationRepo.save(participation);
            storedParticipation = programmingExerciseStudentParticipationRepo.findByExerciseIdAndStudentLogin(exercise.getId(), login);
            assertThat(storedParticipation).isPresent();
            studentParticipation = studentParticipationRepo.findWithEagerSubmissionsAndResultsAssessorsById(storedParticipation.get().getId()).get();
        }
        else {
            studentParticipation = storedParticipation.get();
        }
        return addResultToParticipation(studentParticipation);
    }

    public void addInstructor(final String instructorGroup, final String instructorName) {
        var instructor = ModelFactory.generateActivatedUsers(instructorName, new String[] { instructorGroup, "testgroup" }, instructorAuthorities, 1).get(0);
        instructor = userRepo.save(instructor);

        assertThat(instructor.getId()).as("Instructor has been created").isNotNull();
    }

    public void addTeachingAssistant(final String taGroup, final String taName) {
        var ta = ModelFactory.generateActivatedUsers(taName, new String[] { taGroup, "testgroup" }, tutorAuthorities, 1).get(0);
        ta = userRepo.save(ta);

        assertThat(ta.getId()).as("Teaching assistant has been created").isNotNull();
    }

    public Lecture createCourseWithLecture(boolean saveLecture) {
        Course course = ModelFactory.generateCourse(null, pastTimestamp, futureFutureTimestamp, new HashSet<>(), "tumuser", "tutor", "instructor");

        Lecture lecture = new Lecture();
        lecture.setDescription("Test Lecture");
        lecture.setCourse(course);
        courseRepo.save(course);
        if (saveLecture) {
            lectureRepo.save(lecture);
        }
        return lecture;
    }

    public Course createCourse() {
        Course course = ModelFactory.generateCourse(null, pastTimestamp, futureTimestamp, new HashSet<>(), "tumuser", "tutor", "instructor");
        return courseRepo.save(course);
    }

    public List<Course> createCoursesWithExercisesAndLectures(boolean withParticipations) throws Exception {
        ZonedDateTime pastTimestamp = ZonedDateTime.now().minusDays(5);
        ZonedDateTime futureTimestamp = ZonedDateTime.now().plusDays(5);
        ZonedDateTime futureFutureTimestamp = ZonedDateTime.now().plusDays(8);

        Course course1 = ModelFactory.generateCourse(null, pastTimestamp, futureTimestamp, new HashSet<>(), "tumuser", "tutor", "instructor");
        Course course2 = ModelFactory.generateCourse(null, ZonedDateTime.now().minusDays(8), pastTimestamp, new HashSet<>(), "tumuser", "tutor", "instructor");

        ModelingExercise modelingExercise = ModelFactory.generateModelingExercise(pastTimestamp, futureTimestamp, futureFutureTimestamp, DiagramType.ClassDiagram, course1);
        modelingExercise.setGradingInstructions("some grading instructions");
        addGradingInstructionsToExercise(modelingExercise);
        modelingExercise.getCategories().add("Modeling");
        course1.addExercises(modelingExercise);

        TextExercise textExercise = ModelFactory.generateTextExercise(pastTimestamp, futureTimestamp, futureFutureTimestamp, course1);
        textExercise.setGradingInstructions("some grading instructions");
        addGradingInstructionsToExercise(textExercise);
        textExercise.getCategories().add("Text");
        course1.addExercises(textExercise);

        FileUploadExercise fileUploadExercise = ModelFactory.generateFileUploadExercise(pastTimestamp, futureTimestamp, futureFutureTimestamp, "png", course1);
        fileUploadExercise.setGradingInstructions("some grading instructions");
        addGradingInstructionsToExercise(fileUploadExercise);
        fileUploadExercise.getCategories().add("File");
        course1.addExercises(fileUploadExercise);

        ProgrammingExercise programmingExercise = ModelFactory.generateProgrammingExercise(pastTimestamp, futureTimestamp, course1);
        programmingExercise.setGradingInstructions("some grading instructions");
        addGradingInstructionsToExercise(programmingExercise);
        programmingExercise.getCategories().add("Programming");
        course1.addExercises(programmingExercise);

        QuizExercise quizExercise = ModelFactory.generateQuizExercise(pastTimestamp, futureTimestamp, course1);
        programmingExercise.getCategories().add("Quiz");
        course1.addExercises(quizExercise);

        Lecture lecture1 = ModelFactory.generateLecture(pastTimestamp, futureFutureTimestamp, course1);
        Attachment attachment1 = ModelFactory.generateAttachment(pastTimestamp, lecture1);
        lecture1.addAttachments(attachment1);
        course1.addLectures(lecture1);

        Lecture lecture2 = ModelFactory.generateLecture(pastTimestamp, futureFutureTimestamp, course1);
        Attachment attachment2 = ModelFactory.generateAttachment(pastTimestamp, lecture2);
        lecture2.addAttachments(attachment2);
        course1.addLectures(lecture2);

        course1 = courseRepo.save(course1);
        course2 = courseRepo.save(course2);

        lectureRepo.save(lecture1);
        lectureRepo.save(lecture2);

        attachmentRepo.save(attachment1);
        attachmentRepo.save(attachment2);

        modelingExercise = exerciseRepo.save(modelingExercise);
        textExercise = exerciseRepo.save(textExercise);
        fileUploadExercise = exerciseRepo.save(fileUploadExercise);
        programmingExercise = exerciseRepo.save(programmingExercise);
        quizExercise = exerciseRepo.save(quizExercise);

        if (withParticipations) {

            // create 5 tutor participations and 5 example submissions and connect all of them (to test the many-to-many relationship)
            var tutorParticipations = new ArrayList<TutorParticipation>();
            for (int i = 1; i < 6; i++) {
                var tutorParticipation = new TutorParticipation().tutor(getUserByLogin("tutor" + i));
                tutorParticipationRepo.save(tutorParticipation);
                tutorParticipations.add(tutorParticipation);
            }

            for (int i = 0; i < 5; i++) {
                String validModel = loadFileFromResources("test-data/model-submission/model.54727.json");
                var exampleSubmission = addExampleSubmission(generateExampleSubmission(validModel, modelingExercise, true));
                exampleSubmission.assessmentExplanation("exp");
                for (var tutorParticipation : tutorParticipations) {
                    exampleSubmission.addTutorParticipations(tutorParticipation);
                }
                exampleSubmissionRepo.save(exampleSubmission);
            }

            User user = (userRepo.findOneByLogin("student1")).get();
            StudentParticipation participation1 = ModelFactory.generateStudentParticipation(InitializationState.INITIALIZED, modelingExercise, user);
            StudentParticipation participation2 = ModelFactory.generateStudentParticipation(InitializationState.FINISHED, textExercise, user);
            StudentParticipation participation3 = ModelFactory.generateStudentParticipation(InitializationState.UNINITIALIZED, modelingExercise, user);

            Submission modelingSubmission1 = ModelFactory.generateModelingSubmission("model1", true);
            Submission modelingSubmission2 = ModelFactory.generateModelingSubmission("model2", true);
            Submission textSubmission = ModelFactory.generateTextSubmission("text", Language.ENGLISH, true);

            Result result1 = ModelFactory.generateResult(true, 10);
            Result result2 = ModelFactory.generateResult(true, 12);
            Result result3 = ModelFactory.generateResult(false, 0);

            result1 = resultRepo.save(result1);
            result2 = resultRepo.save(result2);
            result3 = resultRepo.save(result3);

            modelingSubmission1.setResult(result1);
            modelingSubmission2.setResult(result2);
            textSubmission.setResult(result3);

            participation1 = studentParticipationRepo.save(participation1);
            participation2 = studentParticipationRepo.save(participation2);
            participation3 = studentParticipationRepo.save(participation3);

            modelingSubmission1.setParticipation(participation1);
            textSubmission.setParticipation(participation2);
            modelingSubmission2.setParticipation(participation3);

            submissionRepository.save(modelingSubmission1);
            submissionRepository.save(modelingSubmission2);
            submissionRepository.save(textSubmission);
        }

        return Arrays.asList(course1, course2);
    }

    public List<StudentQuestion> createCourseWithExerciseAndStudentQuestions() throws Exception {
        ZonedDateTime pastTimestamp = ZonedDateTime.now().minusDays(5);
        ZonedDateTime futureTimestamp = ZonedDateTime.now().plusDays(5);
        ZonedDateTime futureFutureTimestamp = ZonedDateTime.now().plusDays(8);

        Course course1 = ModelFactory.generateCourse(null, pastTimestamp, futureTimestamp, new HashSet<>(), "tumuser", "tutor", "instructor");

        TextExercise textExercise = ModelFactory.generateTextExercise(pastTimestamp, futureTimestamp, futureFutureTimestamp, course1);
        textExercise.setGradingInstructions("some grading instructions");
        addGradingInstructionsToExercise(textExercise);
        textExercise.getCategories().add("Text");
        course1.addExercises(textExercise);

        courseRepo.save(course1);
        textExercise = exerciseRepo.save(textExercise);

        List<StudentQuestion> studentQuestions = new ArrayList<>();
        StudentQuestion studentQuestion1 = new StudentQuestion();
        studentQuestion1.setExercise(textExercise);
        studentQuestion1.setQuestionText("Test Student Question 1");
        studentQuestion1.setVisibleForStudents(true);
        studentQuestion1.setAuthor(getUserByLogin("student1"));
        studentQuestionRepository.save(studentQuestion1);
        studentQuestions.add(studentQuestion1);

        StudentQuestion studentQuestion2 = new StudentQuestion();
        studentQuestion2.setExercise(textExercise);
        studentQuestion2.setQuestionText("Test Student Question 2");
        studentQuestion2.setVisibleForStudents(true);
        studentQuestion2.setAuthor(getUserByLogin("student2"));
        studentQuestionRepository.save(studentQuestion2);
        studentQuestions.add(studentQuestion2);

        return studentQuestions;
    }

    public Exam addExam(Course course) {
        Exam exam = ModelFactory.generateExam(course);
        examRepository.save(exam);
        return exam;
    }

    public Exam addExam(Course course, User user, ZonedDateTime startDate, ZonedDateTime endDate) {
        Exam exam = ModelFactory.generateExam(course);
        exam.addUser(user);
        exam.setStartDate(startDate);
        exam.setEndDate(endDate);
        examRepository.save(exam);
        return exam;
    }

    public Exam addExamWithExerciseGroup(Course course, boolean mandatory) {
        Exam exam = ModelFactory.generateExam(course);
        ExerciseGroup exerciseGroup = ModelFactory.generateExerciseGroup(mandatory, exam);
        examRepository.save(exam);
        return exam;
    }

    public Exam addActiveExamWithRegisteredUser(Course course, User user) {
        Exam exam = ModelFactory.generateExam(course);
        exam.setStartDate(ZonedDateTime.now().minusHours(1));
        exam.setEndDate(ZonedDateTime.now().plusHours(1));
        exam.addUser(user);
        examRepository.save(exam);
        return exam;
    }

    public StudentExam addStudentExam(Exam exam) {
        StudentExam studentExam = ModelFactory.generateStudentExam(exam);
        studentExamRepository.save(studentExam);
        return studentExam;
    }

    public StudentExam addStudentExamWithExercisesAndParticipationAndSubmission(Exam exam, User user) {
        TextExercise textExercise = ModelFactory.generateTextExerciseForExam(ZonedDateTime.now().minusDays(2), ZonedDateTime.now().plusDays(5), ZonedDateTime.now().plusDays(8),
                null);
        GradingCriterion gradingCriterion = ModelFactory.generateGradingCriterion("title");
        textExercise.addGradingCriteria(gradingCriterion);
        textExercise.setGradingInstructions("this is a grading instruction");
        textExercise.setSampleSolution("this is a sample solution");
        textExercise = exerciseRepo.save(textExercise);

        Submission submission = ModelFactory.generateTextSubmission("", Language.ENGLISH, true);
        addSubmission(textExercise, submission, user.getLogin());

        StudentExam studentExam = ModelFactory.generateStudentExam(exam);
        studentExam.addExercise(textExercise);
        studentExam.setUser(user);
        studentExamRepository.save(studentExam);

        return studentExam;
    }

    /**
     * Stores participation of the user with the given login for the given exercise
     *
     * @param exercise the exercise for which the participation will be created
     * @param login    login of the user
     * @return eagerly loaded representation of the participation object stored in the database
     */
    public StudentParticipation addParticipationForExercise(Exercise exercise, String login) {
        Optional<StudentParticipation> storedParticipation = studentParticipationRepo.findByExerciseIdAndStudentLogin(exercise.getId(), login);
        if (storedParticipation.isEmpty()) {
            User user = getUserByLogin(login);
            StudentParticipation participation = new StudentParticipation();
            participation.setInitializationDate(ZonedDateTime.now());
            participation.setParticipant(user);
            participation.setExercise(exercise);
            studentParticipationRepo.save(participation);
            storedParticipation = studentParticipationRepo.findByExerciseIdAndStudentLogin(exercise.getId(), login);
            assertThat(storedParticipation).isPresent();
        }
        return studentParticipationRepo.findWithEagerSubmissionsAndResultsAssessorsById(storedParticipation.get().getId()).get();
    }

    /**
     * Stores participation of the team with the given id for the given exercise
     *
     * @param exercise the exercise for which the participation will be created
     * @param teamId   id of the team
     * @return eagerly loaded representation of the participation object stored in the database
     */
    public StudentParticipation addTeamParticipationForExercise(Exercise exercise, long teamId) {
        Optional<StudentParticipation> storedParticipation = studentParticipationRepo.findByExerciseIdAndTeamId(exercise.getId(), teamId);
        if (storedParticipation.isEmpty()) {
            Team team = teamRepo.findById(teamId).orElseThrow();
            StudentParticipation participation = new StudentParticipation();
            participation.setInitializationDate(ZonedDateTime.now());
            participation.setParticipant(team);
            participation.setExercise(exercise);
            studentParticipationRepo.save(participation);
            storedParticipation = studentParticipationRepo.findByExerciseIdAndTeamId(exercise.getId(), teamId);
            assertThat(storedParticipation).isPresent();
        }
        return studentParticipationRepo.findWithEagerSubmissionsAndResultsAssessorsById(storedParticipation.get().getId()).get();
    }

    public ProgrammingExerciseStudentParticipation addStudentParticipationForProgrammingExercise(ProgrammingExercise exercise, String login) {

        final var existingParticipation = programmingExerciseStudentParticipationRepo.findByExerciseIdAndStudentLogin(exercise.getId(), login);
        if (existingParticipation.isPresent()) {
            return existingParticipation.get();
        }
        ProgrammingExerciseStudentParticipation participation = preconfigurationOfParticipation(exercise, login);
        final var repoName = (exercise.getProjectKey() + "-" + login).toLowerCase();
        participation.setRepositoryUrl(String.format("http://some.test.url/scm/%s/%s.git", exercise.getProjectKey(), repoName));
        participation = programmingExerciseStudentParticipationRepo.save(participation);

        return (ProgrammingExerciseStudentParticipation) studentParticipationRepo.findWithEagerSubmissionsAndResultsAssessorsById(participation.getId()).get();

    }

    public ProgrammingExerciseStudentParticipation addStudentParticipationForProgrammingExerciseForLocalRepo(ProgrammingExercise exercise, String login, URL localRepoPath) {
        final var existingParticipation = programmingExerciseStudentParticipationRepo.findByExerciseIdAndStudentLogin(exercise.getId(), login);
        if (existingParticipation.isPresent()) {
            return existingParticipation.get();
        }
        ProgrammingExerciseStudentParticipation participation = preconfigurationOfParticipation(exercise, login);
        final var repoName = (exercise.getProjectKey() + "-" + login).toLowerCase();
        participation.setRepositoryUrl(String.format(localRepoPath.toString() + "%s/%s.git", exercise.getProjectKey(), repoName));
        participation = programmingExerciseStudentParticipationRepo.save(participation);

        return (ProgrammingExerciseStudentParticipation) studentParticipationRepo.findWithEagerSubmissionsAndResultsAssessorsById(participation.getId()).get();
    }

    private ProgrammingExerciseStudentParticipation preconfigurationOfParticipation(ProgrammingExercise exercise, String login) {
        final var user = getUserByLogin(login);
        var participation = new ProgrammingExerciseStudentParticipation();
        final var buildPlanId = exercise.getProjectKey().toUpperCase() + "-" + login.toUpperCase();
        participation.setInitializationDate(ZonedDateTime.now());
        participation.setParticipant(user);
        participation.setBuildPlanId(buildPlanId);
        participation.setProgrammingExercise(exercise);
        participation.setInitializationState(InitializationState.INITIALIZED);
        return participation;
    }

    public ProgrammingExercise addTemplateParticipationForProgrammingExercise(ProgrammingExercise exercise) {
        final var repoName = (exercise.getProjectKey() + "-" + RepositoryType.TEMPLATE.getName()).toLowerCase();
        TemplateProgrammingExerciseParticipation participation = new TemplateProgrammingExerciseParticipation();
        participation.setProgrammingExercise(exercise);
        participation.setBuildPlanId(exercise.getProjectKey() + "-" + BuildPlanType.TEMPLATE.getName());
        participation.setRepositoryUrl(String.format("http://some.test.url/scm/%s/%s.git", exercise.getProjectKey(), repoName));
        participation.setInitializationState(InitializationState.INITIALIZED);
        templateProgrammingExerciseParticipationRepo.save(participation);
        exercise.setTemplateParticipation(participation);
        return programmingExerciseRepository.save(exercise);
    }

    public ProgrammingExercise addSolutionParticipationForProgrammingExercise(ProgrammingExercise exercise) {
        final var repoName = (exercise.getProjectKey() + "-" + RepositoryType.SOLUTION.getName()).toLowerCase();
        SolutionProgrammingExerciseParticipation participation = new SolutionProgrammingExerciseParticipation();
        participation.setProgrammingExercise(exercise);
        participation.setBuildPlanId(exercise.getProjectKey() + "-" + BuildPlanType.SOLUTION.getName());
        participation.setRepositoryUrl(String.format("http://some.test.url/scm/%s/%s.git", exercise.getProjectKey(), repoName));
        participation.setInitializationState(InitializationState.INITIALIZED);
        solutionProgrammingExerciseParticipationRepo.save(participation);
        exercise.setSolutionParticipation(participation);
        return programmingExerciseRepository.save(exercise);
    }

    public Result addResultToParticipation(Participation participation) {
        Result result = new Result().participation(participation).resultString("x of y passed").successful(false).rated(true).score(100L);
        return resultRepo.save(result);
    }

    public Result addResultToParticipation(Participation participation, Submission submission) {
        Result result = new Result().participation(participation).resultString("x of y passed").successful(false).score(100L).submission(submission);
        return resultRepo.save(result);
    }

    public Result addSampleFeedbackToResults(Result result) {
        Feedback feedback1 = feedbackRepo.save(new Feedback().detailText("detail1"));
        Feedback feedback2 = feedbackRepo.save(new Feedback().detailText("detail2"));
        List<Feedback> feedbacks = new ArrayList<>();
        feedbacks.add(feedback1);
        feedbacks.add(feedback2);
        result.addFeedbacks(feedbacks);
        return resultRepo.save(result);
    }

    public Result addResultToSubmission(Submission submission) {
        Result result = new Result().participation(submission.getParticipation()).submission(submission).resultString("x of y passed").rated(true).score(100L);
        resultRepo.save(result);
        return result;
    }

    public List<GradingCriterion> addGradingInstructionsToExercise(Exercise exercise) {
        GradingCriterion emptyCriterion = ModelFactory.generateGradingCriterion(null);
        List<GradingInstruction> instructionWithNoCriteria = ModelFactory.generateGradingInstructions(emptyCriterion, 1);
        emptyCriterion.setExercise(exercise);
        emptyCriterion.setStructuredGradingInstructions(instructionWithNoCriteria);
        GradingCriterion testCriterion = ModelFactory.generateGradingCriterion("test title");
        List<GradingInstruction> instructions = ModelFactory.generateGradingInstructions(testCriterion, 3);
        testCriterion.setStructuredGradingInstructions(instructions);
        testCriterion.setExercise(exercise);
        var criteria = new ArrayList<GradingCriterion>();
        criteria.add(emptyCriterion);
        criteria.add(testCriterion);
        exercise.setGradingCriteria(criteria);
        return exercise.getGradingCriteria();
    }

    public Course addCourseWithOneModelingExercise() {
        long currentCourseRepoSize = courseRepo.count();
        long currentExerciseRepoSize = exerciseRepo.count();
        Course course = ModelFactory.generateCourse(null, pastTimestamp, futureFutureTimestamp, new HashSet<>(), "tumuser", "tutor", "instructor");
        ModelingExercise modelingExercise = ModelFactory.generateModelingExercise(pastTimestamp, futureTimestamp, futureFutureTimestamp, DiagramType.ClassDiagram, course);
        modelingExercise.setTitle("ClassDiagram");
        course.addExercises(modelingExercise);
        course.setMaxComplaintTimeDays(14);
        course = courseRepo.save(course);
        modelingExercise = exerciseRepo.save(modelingExercise);
        assertThat(exerciseRepo.count()).as("one exercise got stored").isEqualTo(currentExerciseRepoSize + 1L);
        assertThat(courseRepo.count()).as("a course got stored").isEqualTo(currentCourseRepoSize + 1L);
        assertThat(course.getExercises()).as("course contains the exercise").containsExactlyInAnyOrder(modelingExercise);
        assertThat(modelingExercise.getPresentationScoreEnabled()).as("presentation score is enabled").isTrue();
        return course;
    }

    public Course addCourseWithOneReleasedTextExercise() {
        Course course = ModelFactory.generateCourse(null, pastTimestamp, futureFutureTimestamp, new HashSet<>(), "tumuser", "tutor", "instructor");
        TextExercise textExercise = ModelFactory.generateTextExercise(pastTimestamp, futureTimestamp, futureFutureTimestamp, course);
        textExercise.setTitle("Text");
        course.addExercises(textExercise);
        final var exercisesNrBefore = exerciseRepo.count();
        final var courseNrBefore = courseRepo.count();
        courseRepo.save(course);
        exerciseRepo.save(textExercise);
        assertThat(exercisesNrBefore + 1).as("one exercise got stored").isEqualTo(exerciseRepo.count());
        assertThat(courseNrBefore + 1).as("a course got stored").isEqualTo(courseRepo.count());
        assertThat(courseRepo.findWithEagerExercisesById(course.getId()).getExercises()).as("course contains the exercise").contains(textExercise);
        assertThat(textExercise.getPresentationScoreEnabled()).as("presentation score is enabled").isTrue();

        return course;
    }

    public ProgrammingExercise addCourseExamExerciseGroupWithOneProgrammingExerciseAndTestCases() {
        ExerciseGroup exerciseGroup = addExerciseGroupWithExamAndCourse(true);
        ProgrammingExercise programmingExercise = new ProgrammingExercise();
        programmingExercise.setExerciseGroup(exerciseGroup);
        populateProgrammingExercise(programmingExercise, "TESTEXFOREXAM");

        programmingExercise = programmingExerciseRepository.save(programmingExercise);
        programmingExercise = addSolutionParticipationForProgrammingExercise(programmingExercise);
        programmingExercise = addTemplateParticipationForProgrammingExercise(programmingExercise);

        addTestCasesToProgrammingExercise(programmingExercise);
        return programmingExercise;
    }

    public TextExercise addCourseExamExerciseGroupWithOneTextExercise() {
        var now = ZonedDateTime.now();
        ExerciseGroup exerciseGroup = addExerciseGroupWithExamAndCourse(true);
        TextExercise textExercise = ModelFactory.generateTextExerciseForExam(now.minusDays(1), now.minusHours(2), now.minusHours(1), exerciseGroup);
        final var exercisesNrBefore = exerciseRepo.count();
        exerciseRepo.save(textExercise);
        assertThat(exercisesNrBefore + 1).as("one exercise got stored").isEqualTo(exerciseRepo.count());
        return textExercise;
    }

    public FileUploadExercise addCourseExamExerciseGroupWithOneFileUploadExercise() {
        var now = ZonedDateTime.now();
        ExerciseGroup exerciseGroup = addExerciseGroupWithExamAndCourse(true);
        FileUploadExercise fileUploadExercise = ModelFactory.generateFileUploadExerciseForExam(now.minusDays(1), now.minusHours(2), now.minusHours(1), "pdf", exerciseGroup);
        final var exercisesNrBefore = exerciseRepo.count();
        exerciseRepo.save(fileUploadExercise);
        assertThat(exercisesNrBefore + 1).as("one exercise got stored").isEqualTo(exerciseRepo.count());
        return fileUploadExercise;
    }

    public ExerciseGroup addExerciseGroupWithExamAndCourse(boolean mandatory) {
        Course course = ModelFactory.generateCourse(null, pastTimestamp, futureFutureTimestamp, new HashSet<>(), "tumuser", "tutor", "instructor");
        Exam exam = ModelFactory.generateExam(course);
        ExerciseGroup exerciseGroup = ModelFactory.generateExerciseGroup(mandatory, exam);
        final var courseNrBefore = courseRepo.count();
        final var examNrBefore = examRepository.count();
        final var exerciseGroupNrBefore = exerciseGroupRepository.count();

        courseRepo.save(course);
        examRepository.save(exam);

        assertThat(courseNrBefore + 1).as("a course got stored").isEqualTo(courseRepo.count());
        assertThat(examNrBefore + 1).as("an exam got stored").isEqualTo(examRepository.count());
        assertThat(exerciseGroupNrBefore + 1).as("an exerciseGroup got stored").isEqualTo(exerciseGroupRepository.count());

        Optional<Course> optionalCourse = courseRepo.findById(course.getId());
        assertThat(optionalCourse).as("course can be retrieved").isPresent();
        Course courseDB = optionalCourse.get();

        Optional<Exam> optionalExam = examRepository.findById(exam.getId());
        assertThat(optionalCourse).as("exam can be retrieved").isPresent();
        Exam examDB = optionalExam.get();

        Optional<ExerciseGroup> optionalExerciseGroup = exerciseGroupRepository.findById(exerciseGroup.getId());
        assertThat(optionalExerciseGroup).as("exerciseGroup can be retrieved").isPresent();
        ExerciseGroup exerciseGroupDB = optionalExerciseGroup.get();

        assertThat(examDB.getCourse().getId()).as("exam and course are linked correctly").isEqualTo(courseDB.getId());
        assertThat(exerciseGroupDB.getExam().getId()).as("exerciseGroup and exam are linked correctly").isEqualTo(examDB.getId());

        return exerciseGroup;
    }

    public Course addCourseWithOneFinishedTextExercise() {
        long numberOfCourses = courseRepo.count();
        long numberOfExercises = exerciseRepo.count();

        Course course = ModelFactory.generateCourse(null, pastTimestamp, futureFutureTimestamp, new HashSet<>(), "tumuser", "tutor", "instructor");
        TextExercise finishedTextExercise = ModelFactory.generateTextExercise(pastTimestamp, pastTimestamp.plusHours(12), pastTimestamp.plusHours(24), course);
        finishedTextExercise.setTitle("Finished");
        course.addExercises(finishedTextExercise);
        courseRepo.save(course);
        exerciseRepo.save(finishedTextExercise);
        List<Course> courseRepoContent = courseRepo.findAllActiveWithEagerExercisesAndLectures(ZonedDateTime.now());
        List<Exercise> exerciseRepoContent = exerciseRepo.findAll();
        assertThat(exerciseRepoContent.size()).as("one exercise got stored").isEqualTo(numberOfExercises + 1);
        assertThat(courseRepoContent.size()).as("a course got stored").isEqualTo(numberOfCourses + 1);
        return course;
    }

    public void addCourseWithOneModelingAndOneTextExercise() {
        long numberOfCourses = courseRepo.count();
        long numberOfExercises = exerciseRepo.count();
        Course course = ModelFactory.generateCourse(null, pastTimestamp, futureFutureTimestamp, new HashSet<>(), "tumuser", "tutor", "instructor");
        ModelingExercise modelingExercise = ModelFactory.generateModelingExercise(pastTimestamp, futureTimestamp, futureFutureTimestamp, DiagramType.ClassDiagram, course);
        TextExercise textExercise = ModelFactory.generateTextExercise(pastTimestamp, futureTimestamp, futureFutureTimestamp, course);
        course.addExercises(modelingExercise);
        course.addExercises(textExercise);
        courseRepo.save(course);
        exerciseRepo.save(modelingExercise);
        exerciseRepo.save(textExercise);
        List<Course> courseRepoContent = courseRepo.findAllActiveWithEagerExercisesAndLectures(ZonedDateTime.now());
        List<Exercise> exerciseRepoContent = exerciseRepo.findAll();
        assertThat(exerciseRepoContent.size()).as("two exercises got stored").isEqualTo(numberOfExercises + 2);
        assertThat(courseRepoContent.size()).as("a course got stored").isEqualTo(numberOfCourses + 1);
    }

    public Course addCourseWithDifferentModelingExercises() {
        Course course = ModelFactory.generateCourse(null, pastTimestamp, futureFutureTimestamp, new HashSet<>(), "tumuser", "tutor", "instructor");
        ModelingExercise classExercise = ModelFactory.generateModelingExercise(pastTimestamp, futureTimestamp, futureFutureTimestamp, DiagramType.ClassDiagram, course);
        classExercise.setTitle("ClassDiagram");
        course.addExercises(classExercise);

        ModelingExercise activityExercise = ModelFactory.generateModelingExercise(pastTimestamp, futureTimestamp, futureFutureTimestamp, DiagramType.ActivityDiagram, course);
        activityExercise.setTitle("ActivityDiagram");
        course.addExercises(activityExercise);

        ModelingExercise objectExercise = ModelFactory.generateModelingExercise(pastTimestamp, futureTimestamp, futureFutureTimestamp, DiagramType.ObjectDiagram, course);
        objectExercise.setTitle("ObjectDiagram");
        course.addExercises(objectExercise);

        ModelingExercise useCaseExercise = ModelFactory.generateModelingExercise(pastTimestamp, futureTimestamp, futureFutureTimestamp, DiagramType.UseCaseDiagram, course);
        useCaseExercise.setTitle("UseCaseDiagram");
        course.addExercises(useCaseExercise);

        ModelingExercise finishedExercise = ModelFactory.generateModelingExercise(pastTimestamp, pastTimestamp, futureTimestamp, DiagramType.ClassDiagram, course);
        finishedExercise.setTitle("finished");
        course.addExercises(finishedExercise);

        course = courseRepo.save(course);
        exerciseRepo.save(classExercise);
        exerciseRepo.save(activityExercise);
        exerciseRepo.save(objectExercise);
        exerciseRepo.save(useCaseExercise);
        exerciseRepo.save(finishedExercise);
        Course storedCourse = courseRepo.findWithEagerExercisesAndLecturesById(course.getId());
        Set<Exercise> exercises = storedCourse.getExercises();
        assertThat(exercises.size()).as("five exercises got stored").isEqualTo(5);
        assertThat(exercises).as("Contains all exercises").containsExactlyInAnyOrder(course.getExercises().toArray(new Exercise[] {}));
        return course;
    }

    public Course addCourseWithOneProgrammingExercise() {
        var course = ModelFactory.generateCourse(null, pastTimestamp, futureFutureTimestamp, new HashSet<>(), "tumuser", "tutor", "instructor");
        course = courseRepo.save(course);

        var programmingExercise = (ProgrammingExercise) new ProgrammingExercise().course(course);
        populateProgrammingExercise(programmingExercise, "TSTEXC");
        programmingExercise.setPresentationScoreEnabled(course.getPresentationScore() != 0);

        programmingExercise = programmingExerciseRepository.save(programmingExercise);
        course.addExercises(programmingExercise);
        programmingExercise = addSolutionParticipationForProgrammingExercise(programmingExercise);
        programmingExercise = addTemplateParticipationForProgrammingExercise(programmingExercise);

        assertThat(programmingExercise.getPresentationScoreEnabled()).as("presentation score is enabled").isTrue();

        return courseRepo.findWithEagerExercisesAndLecturesById(course.getId());
    }

    private void populateProgrammingExercise(ProgrammingExercise programmingExercise, String shortName) {
        programmingExercise.setProgrammingLanguage(ProgrammingLanguage.JAVA);
        programmingExercise.setShortName(shortName);
        programmingExercise.generateAndSetProjectKey();
        programmingExercise.setReleaseDate(ZonedDateTime.now().plusDays(1));
        programmingExercise.setBuildAndTestStudentSubmissionsAfterDueDate(ZonedDateTime.now().plusDays(5));
        programmingExercise.setPublishBuildPlanUrl(true);
        programmingExercise.setMaxScore(42.0);
        programmingExercise.setDifficulty(DifficultyLevel.EASY);
        programmingExercise.setMode(ExerciseMode.INDIVIDUAL);
        programmingExercise.setProblemStatement("Lorem Ipsum");
        programmingExercise.setAssessmentType(AssessmentType.AUTOMATIC);
        programmingExercise.setGradingInstructions("Lorem Ipsum");
        programmingExercise.setTitle("Programming");
        programmingExercise.setAllowOnlineEditor(true);
        programmingExercise.setPackageName("de.test");
        programmingExercise.setDueDate(ZonedDateTime.now().plusDays(2));
        programmingExercise.setAssessmentDueDate(ZonedDateTime.now().plusDays(3));
        programmingExercise.setCategories(new HashSet<>(Set.of("cat1", "cat2")));
        programmingExercise.setTestRepositoryUrl("http://nadnasidni.tum/scm/" + programmingExercise.getProjectKey() + "/" + programmingExercise.getProjectKey() + "-tests.git");
    }

    public Course addEmptyCourse() {
        Course course = ModelFactory.generateCourse(null, pastTimestamp, futureFutureTimestamp, new HashSet<>(), "tumuser", "tutor", "instructor");
        courseRepo.save(course);

        assertThat(courseRepo.findById(course.getId())).as("empty course is initialized").isPresent();

        return course;
    }

    public Course addCourseWithOneProgrammingExerciseAndSpecificTestCases() {
        Course course = addCourseWithOneProgrammingExercise();
        ProgrammingExercise programmingExercise = findProgrammingExerciseWithTitle(course.getExercises(), "Programming");

        List<ProgrammingExerciseTestCase> testCases = new ArrayList<>();
        testCases.add(new ProgrammingExerciseTestCase().testName("testClass[BubbleSort]").weight(1).active(true).exercise(programmingExercise).afterDueDate(false));
        testCases.add(new ProgrammingExerciseTestCase().testName("testMethods[Context]").weight(2).active(true).exercise(programmingExercise).afterDueDate(false));
        testCases.add(new ProgrammingExerciseTestCase().testName("testMethods[Policy]").weight(3).active(true).exercise(programmingExercise).afterDueDate(false));
        testCaseRepository.saveAll(testCases);

        List<ProgrammingExerciseTestCase> tests = new ArrayList<>(testCaseRepository.findByExerciseId(programmingExercise.getId()));
        assertThat(tests).as("test case is initialized").hasSize(3);

        return courseRepo.findById(course.getId()).get();
    }

    public Course addCourseWithOneProgrammingExerciseAndTestCases() {
        Course course = addCourseWithOneProgrammingExercise();
        ProgrammingExercise programmingExercise = findProgrammingExerciseWithTitle(course.getExercises(), "Programming");

        addTestCasesToProgrammingExercise(programmingExercise);

        return courseRepo.findById(course.getId()).get();
    }

    private void addTestCasesToProgrammingExercise(ProgrammingExercise programmingExercise) {
        List<ProgrammingExerciseTestCase> testCases = new ArrayList<>();
        testCases.add(new ProgrammingExerciseTestCase().testName("test1").weight(1).active(true).exercise(programmingExercise).afterDueDate(false));
        testCases.add(new ProgrammingExerciseTestCase().testName("test2").weight(2).active(false).exercise(programmingExercise).afterDueDate(false));
        testCases.add(new ProgrammingExerciseTestCase().testName("test3").weight(3).active(true).exercise(programmingExercise).afterDueDate(true));
        testCaseRepository.saveAll(testCases);

        List<ProgrammingExerciseTestCase> tests = new ArrayList<>(testCaseRepository.findByExerciseId(programmingExercise.getId()));
        assertThat(tests).as("test case is initialized").hasSize(3);
    }

    public Course addCourseWithModelingAndTextExercise() {
        Course course = ModelFactory.generateCourse(null, pastTimestamp, futureFutureTimestamp, new HashSet<>(), "tumuser", "tutor", "instructor");
        ModelingExercise modelingExercise = ModelFactory.generateModelingExercise(pastTimestamp, futureTimestamp, futureFutureTimestamp, DiagramType.ClassDiagram, course);
        modelingExercise.setTitle("Modeling");
        course.addExercises(modelingExercise);
        TextExercise textExercise = ModelFactory.generateTextExercise(pastTimestamp, futureTimestamp, futureFutureTimestamp, course);
        textExercise.setTitle("Text");
        course.addExercises(textExercise);
        course = courseRepo.save(course);
        exerciseRepo.save(modelingExercise);
        exerciseRepo.save(textExercise);
        return course;
    }

    public List<FileUploadExercise> createFileUploadExercisesWithCourse() {
        Course course = ModelFactory.generateCourse(null, pastTimestamp, futureFutureTimestamp, new HashSet<>(), "tumuser", "tutor", "instructor");
        courseRepo.save(course);
        List<Course> courseRepoContent = courseRepo.findAllActiveWithEagerExercisesAndLectures(ZonedDateTime.now());
        assertThat(courseRepoContent.size()).as("a course got stored").isEqualTo(1);

        FileUploadExercise releasedFileUploadExercise = ModelFactory.generateFileUploadExercise(pastTimestamp, futureTimestamp, futureFutureTimestamp, "png,pdf", course);
        releasedFileUploadExercise.setTitle("released");
        FileUploadExercise finishedFileUploadExercise = ModelFactory.generateFileUploadExercise(pastTimestamp, pastTimestamp, futureFutureTimestamp, "png,pdf", course);
        finishedFileUploadExercise.setTitle("finished");
        FileUploadExercise assessedFileUploadExercise = ModelFactory.generateFileUploadExercise(pastTimestamp, pastTimestamp, pastTimestamp, "png,pdf", course);
        assessedFileUploadExercise.setTitle("assessed");

        var fileUploadExercises = new ArrayList<FileUploadExercise>();
        fileUploadExercises.add(releasedFileUploadExercise);
        fileUploadExercises.add(finishedFileUploadExercise);
        fileUploadExercises.add(assessedFileUploadExercise);
        return fileUploadExercises;
    }

    public Course addCourseWithThreeFileUploadExercise() {
        var fileUploadExercises = createFileUploadExercisesWithCourse();
        exerciseRepo.saveAll(fileUploadExercises);
        List<Course> courseRepoContent = courseRepo.findAllActiveWithEagerExercisesAndLectures(ZonedDateTime.now());
        List<Exercise> exerciseRepoContent = exerciseRepo.findAll();
        assertThat(exerciseRepoContent.size()).as("one exercise got stored").isEqualTo(3);
        assertThat(courseRepoContent.size()).as("a course got stored").isEqualTo(1);
        assertThat(courseRepoContent.get(0).getExercises()).as("course contains the exercises").containsExactlyInAnyOrder(exerciseRepoContent.toArray(new Exercise[] {}));
        return courseRepoContent.get(0);
    }

    /**
     * Stores for the given model a submission of the user and initiates the corresponding Result
     *
     * @param exercise exercise the submission belongs to
     * @param model    ModelingSubmission json as string contained in the submission
     * @param login    of the user the submission belongs to
     * @return submission stored in the modelingSubmissionRepository
     */
    public ModelingSubmission addModelingSubmissionWithEmptyResult(ModelingExercise exercise, String model, String login) {
        StudentParticipation participation = addParticipationForExercise(exercise, login);
        ModelingSubmission submission = ModelFactory.generateModelingSubmission(model, true);
        submission = modelSubmissionService.save(submission, exercise, login);
        Result result = new Result();
        result = resultRepo.save(result);
        result.setSubmission(submission);
        submission.setResult(result);
        participation.addResult(result);
        studentParticipationRepo.save(participation);
        submission = modelingSubmissionRepo.save(submission);
        result = resultRepo.save(result);
        return submission;
    }

    public ModelingSubmission addModelingSubmission(ModelingExercise exercise, ModelingSubmission submission, String login) {
        StudentParticipation participation = addParticipationForExercise(exercise, login);
        participation.addSubmissions(submission);
        submission.setParticipation(participation);
        modelingSubmissionRepo.save(submission);
        studentParticipationRepo.save(participation);
        return submission;
    }

    public ModelingSubmission addModelingTeamSubmission(ModelingExercise exercise, ModelingSubmission submission, Team team) {
        StudentParticipation participation = addTeamParticipationForExercise(exercise, team.getId());
        participation.addSubmissions(submission);
        submission.setParticipation(participation);
        modelingSubmissionRepo.save(submission);
        studentParticipationRepo.save(participation);
        return submission;
    }

    public ProgrammingSubmission addProgrammingSubmission(ProgrammingExercise exercise, ProgrammingSubmission submission, String login) {
        StudentParticipation participation = addStudentParticipationForProgrammingExercise(exercise, login);
        submission.setParticipation(participation);
        submission.setParticipation(participation);
        programmingSubmissionRepo.save(submission);
        return submission;
    }

    /**
     * Add a submission with a result to the given programming exercise. The submission will be assigned to the corresponding participation of the given login (if exists or create a new participation).
     * The method will make sure that all necessary entities are connected.
     *
     * @param exercise for which to create the submission/participation/result combination.
     * @param submission to use for adding to the exercise/participation/result.
     * @param login of the user to identify the corresponding student participation.
     * @return the updated programming submission that is linked to all related entities.
     */
    public ProgrammingSubmission addProgrammingSubmissionWithResult(ProgrammingExercise exercise, ProgrammingSubmission submission, String login) {
        StudentParticipation participation = addStudentParticipationForProgrammingExercise(exercise, login);
        Result result = resultRepo.save(new Result().participation(participation));
        participation.addSubmissions(submission);
        submission.setParticipation(participation);
        submission.setResult(result);
        submission = programmingSubmissionRepo.save(submission);
        result.setSubmission(submission);
        result = resultRepo.save(result);
        participation.addResult(result);
        studentParticipationRepo.save(participation);
        return submission;
    }

    public ProgrammingSubmission addProgrammingSubmissionWithResult(ProgrammingExercise exercise, ProgrammingSubmission submission, Result result, String login) {
        StudentParticipation participation = addStudentParticipationForProgrammingExercise(exercise, login);
        participation.addSubmissions(submission);
        submission.setParticipation(participation);
        submission.setResult(result);
        programmingSubmissionRepo.save(submission);
        participation.addResult(result);
        studentParticipationRepo.save(participation);
        return submission;
    }

    public ProgrammingSubmission addProgrammingSubmissionWithResultAndAssessor(ProgrammingExercise exercise, ProgrammingSubmission submission, String login, String assessorLogin) {
        StudentParticipation participation = addParticipationForExercise(exercise, login);
        participation.addSubmissions(submission);
        Result result = new Result();
        result.setAssessor(getUserByLogin(assessorLogin));
        result.setAssessmentType(AssessmentType.MANUAL);
        result.setScore(50L);
        result.setCompletionDate(ZonedDateTime.now());
        result = resultRepo.save(result);
        result.setSubmission(submission);
        submission.setParticipation(participation);
        submission.setResult(result);
        submission.getParticipation().addResult(result);
        submission = programmingSubmissionRepo.save(submission);
        result = resultRepo.save(result);
        studentParticipationRepo.save(participation);
        return submission;
    }

    public Submission addSubmission(Exercise exercise, Submission submission, String login) {
        StudentParticipation participation = addParticipationForExercise(exercise, login);
        participation.addSubmissions(submission);
        submission.setParticipation(participation);
        submissionRepository.save(submission);
        studentParticipationRepo.save(participation);
        return submission;
    }

    public Submission addSubmission(StudentParticipation participation, Submission submission, String login) {
        participation.addSubmissions(submission);
        submission.setParticipation(participation);
        submissionRepository.save(submission);
        studentParticipationRepo.save(participation);
        return submission;
    }

    public ModelingSubmission addModelingSubmissionWithResultAndAssessor(ModelingExercise exercise, ModelingSubmission submission, String login, String assessorLogin) {
        StudentParticipation participation = addParticipationForExercise(exercise, login);
        participation.addSubmissions(submission);
        Result result = new Result();
        result.setAssessor(getUserByLogin(assessorLogin));
        result.setAssessmentType(AssessmentType.MANUAL);
        result = resultRepo.save(result);
        result.setSubmission(submission);
        submission.setParticipation(participation);
        submission.setResult(result);
        submission.getParticipation().addResult(result);
        submission = modelingSubmissionRepo.save(submission);
        result = resultRepo.save(result);
        studentParticipationRepo.save(participation);
        return submission;
    }

    public ModelingSubmission addModelingSubmissionWithFinishedResultAndAssessor(ModelingExercise exercise, ModelingSubmission submission, String login, String assessorLogin) {
        StudentParticipation participation = addParticipationForExercise(exercise, login);
        participation.addSubmissions(submission);
        Result result = new Result();
        result.setAssessor(getUserByLogin(assessorLogin));
        result.setCompletionDate(ZonedDateTime.now());
        result = resultRepo.save(result);
        result.setSubmission(submission);
        submission.setParticipation(participation);
        submission.setResult(result);
        submission.getParticipation().addResult(result);
        submission = modelingSubmissionRepo.save(submission);
        result = resultRepo.save(result);
        studentParticipationRepo.save(participation);
        return submission;
    }

    public FileUploadSubmission addFileUploadSubmission(FileUploadExercise fileUploadExercise, FileUploadSubmission fileUploadSubmission, String login) {
        StudentParticipation participation = addParticipationForExercise(fileUploadExercise, login);
        participation.addSubmissions(fileUploadSubmission);
        fileUploadSubmission.setParticipation(participation);
        fileUploadSubmissionRepo.save(fileUploadSubmission);
        studentParticipationRepo.save(participation);
        return fileUploadSubmission;
    }

    public FileUploadSubmission addFileUploadSubmissionWithResultAndAssessorFeedback(FileUploadExercise fileUploadExercise, FileUploadSubmission fileUploadSubmission, String login,
            String assessorLogin, List<Feedback> feedbacks) {
        StudentParticipation participation = addParticipationForExercise(fileUploadExercise, login);
        participation.addSubmissions(fileUploadSubmission);
        Result result = new Result();
        result.setAssessor(getUserByLogin(assessorLogin));
        result.setScore(100L);
        result.setCompletionDate(fileUploadExercise.getReleaseDate());
        result.setFeedbacks(feedbacks);
        result = resultRepo.save(result);
        result.setSubmission(fileUploadSubmission);
        fileUploadSubmission.setParticipation(participation);
        fileUploadSubmission.setResult(result);
        fileUploadSubmission.getParticipation().addResult(result);
        fileUploadSubmission = fileUploadSubmissionRepo.save(fileUploadSubmission);
        result = resultRepo.save(result);
        studentParticipationRepo.save(participation);
        return fileUploadSubmission;
    }

    public FileUploadSubmission addFileUploadSubmissionWithResultAndAssessor(FileUploadExercise fileUploadExercise, FileUploadSubmission fileUploadSubmission, String login,
            String assessorLogin) {
        return addFileUploadSubmissionWithResultAndAssessorFeedback(fileUploadExercise, fileUploadSubmission, login, assessorLogin, new ArrayList<>());
    }

    public TextSubmission addTextSubmission(TextExercise exercise, TextSubmission submission, String login) {
        StudentParticipation participation = addParticipationForExercise(exercise, login);
        participation.addSubmissions(submission);
        submission.setParticipation(participation);
        submission = textSubmissionRepo.save(submission);
        studentParticipationRepo.save(participation);
        return submission;
    }

    private TextSubmission addTextSubmissionWithResultAndAssessor(TextExercise exercise, TextSubmission submission, String studentLogin, Long teamId, String assessorLogin) {
        StudentParticipation participation = Optional.ofNullable(studentLogin).map(login -> addParticipationForExercise(exercise, login))
                .orElseGet(() -> addTeamParticipationForExercise(exercise, teamId));
        participation.addSubmissions(submission);
        Result result = new Result();
        result.setAssessor(getUserByLogin(assessorLogin));
        result.setScore(100L);
        result.setCompletionDate(exercise.getReleaseDate());
        result = resultRepo.save(result);
        result.setSubmission(submission);
        submission.setParticipation(participation);
        submission.setResult(result);
        submission.getParticipation().addResult(result);
        submission = textSubmissionRepo.save(submission);
        result = resultRepo.save(result);
        studentParticipationRepo.save(participation);
        submission.setResult(result);
        return submission;
    }

    public TextSubmission addTextSubmissionWithResultAndAssessor(TextExercise exercise, TextSubmission submission, String login, String assessorLogin) {
        return addTextSubmissionWithResultAndAssessor(exercise, submission, login, null, assessorLogin);
    }

    public TextSubmission addTextSubmissionWithResultAndAssessor(TextExercise exercise, TextSubmission submission, long teamId, String assessorLogin) {
        return addTextSubmissionWithResultAndAssessor(exercise, submission, null, teamId, assessorLogin);
    }

    public TextSubmission addTextSubmissionWithResultAndAssessorAndFeedbacks(TextExercise exercise, TextSubmission submission, String studentLogin, String assessorLogin,
            List<Feedback> feedbacks) {
        submission = addTextSubmissionWithResultAndAssessor(exercise, submission, studentLogin, null, assessorLogin);
        Result result = submission.getResult();
        for (Feedback f : feedbacks)
            f.setResult(result);
        feedbackRepo.saveAll(feedbacks);
        result.setFeedbacks(feedbacks);
        resultRepo.save(result);
        return submission;
    }

    public TextSubmission addTextBlocksToTextSubmission(List<TextBlock> blocks, TextSubmission submission) {
        blocks.forEach(block -> {
            block.setSubmission(submission);
            block.setTextFromSubmission();
            block.computeId();
        });
        submission.setBlocks(blocks);
        textBlockRepo.saveAll(blocks);
        textSubmissionRepo.save(submission);
        return submission;
    }

    public ModelingSubmission addModelingSubmissionFromResources(ModelingExercise exercise, String path, String login) throws Exception {
        String model = loadFileFromResources(path);
        ModelingSubmission submission = ModelFactory.generateModelingSubmission(model, true);
        submission = addModelingSubmission(exercise, submission, login);
        checkModelingSubmissionCorrectlyStored(submission.getId(), model);
        return submission;
    }

    public void checkModelingSubmissionCorrectlyStored(Long submissionId, String sentModel) throws Exception {
        Optional<ModelingSubmission> modelingSubmission = modelingSubmissionRepo.findById(submissionId);
        assertThat(modelingSubmission).as("submission correctly stored").isPresent();
        checkModelsAreEqual(modelingSubmission.get().getModel(), sentModel);
    }

    public void checkModelsAreEqual(String storedModel, String sentModel) throws Exception {
        JsonObject sentModelObject = parseString(sentModel).getAsJsonObject();
        JsonObject storedModelObject = parseString(storedModel).getAsJsonObject();
        assertThat(storedModelObject).as("model correctly stored").isEqualTo(sentModelObject);
    }

    public Result addModelingAssessmentForSubmission(ModelingExercise exercise, ModelingSubmission submission, String path, String login, boolean submit) throws Exception {
        List<Feedback> assessment = loadAssessmentFomResources(path);
        Result result = modelingAssessmentService.saveManualAssessment(submission, assessment, exercise);
        result.setParticipation(submission.getParticipation().results(null));
        result.setAssessor(getUserByLogin(login));
        resultRepo.save(result);
        if (submit) {
            modelingAssessmentService.submitManualAssessment(result.getId(), exercise, submission.getSubmissionDate());
        }
        return resultRepo.findWithEagerSubmissionAndFeedbackAndAssessorById(result.getId()).get();
    }

    public ExampleSubmission addExampleSubmission(ExampleSubmission exampleSubmission) {
        modelingSubmissionRepo.save((ModelingSubmission) exampleSubmission.getSubmission());
        return exampleSubmissionRepo.save(exampleSubmission);
    }

    /**
     * @param path path relative to the test resources foldercomplaint
     * @return string representation of given file
     * @throws Exception
     */
    public String loadFileFromResources(String path) throws Exception {
        java.io.File file = ResourceUtils.getFile("classpath:" + path);
        StringBuilder builder = new StringBuilder();
        Files.lines(file.toPath()).forEach(builder::append);
        assertThat(builder.toString()).as("model has been correctly read from file").isNotEqualTo("");
        return builder.toString();
    }

    public List<Feedback> loadAssessmentFomResources(String path) throws Exception {
        String fileContent = loadFileFromResources(path);
        List<Feedback> modelingAssessment = mapper.readValue(fileContent, mapper.getTypeFactory().constructCollectionType(List.class, Feedback.class));
        return modelingAssessment;
    }

    public User getUserByLogin(String login) {
        return userRepo.findOneWithAuthoritiesByLogin(login).orElseThrow(() -> new IllegalArgumentException("Provided login " + login + " does not exist in database"));
    }

    public void updateExerciseDueDate(long exerciseId, ZonedDateTime newDueDate) {
        Exercise exercise = exerciseRepo.findById(exerciseId).orElseThrow(() -> new IllegalArgumentException("Exercise with given ID " + exerciseId + " could not be found"));
        exercise.setDueDate(newDueDate);
        exerciseRepo.save(exercise);
    }

    public void updateAssessmentDueDate(long exerciseId, ZonedDateTime newDueDate) {
        Exercise exercise = exerciseRepo.findById(exerciseId).orElseThrow(() -> new IllegalArgumentException("Exercise with given ID " + exerciseId + " could not be found"));
        exercise.setAssessmentDueDate(newDueDate);
        exerciseRepo.save(exercise);
    }

    public void updateResultCompletionDate(long resultId, ZonedDateTime newCompletionDate) {
        Result result = resultRepo.findById(resultId).orElseThrow(() -> new IllegalArgumentException("Result with given ID " + resultId + " could not be found"));
        result.setCompletionDate(newCompletionDate);
        resultRepo.save(result);
    }

    public void addComplaints(String studentLogin, Participation participation, int numberOfComplaints, ComplaintType complaintType) {
        for (int i = 0; i < numberOfComplaints; i++) {
            Result dummyResult = new Result().participation(participation);
            dummyResult = resultRepo.save(dummyResult);
            Complaint complaint = new Complaint().participant(getUserByLogin(studentLogin)).result(dummyResult).complaintType(complaintType);
            complaintRepo.save(complaint);
        }
    }

    public void addTeamComplaints(Team team, Participation participation, int numberOfComplaints, ComplaintType complaintType) {
        for (int i = 0; i < numberOfComplaints; i++) {
            Result dummyResult = new Result().participation(participation);
            dummyResult = resultRepo.save(dummyResult);
            Complaint complaint = new Complaint().participant(team).result(dummyResult).complaintType(complaintType);
            complaintRepo.save(complaint);
        }
    }

    public Result addResultToSubmission(Submission submission, AssessmentType assessmentType, User user, Long score, boolean rated) {
        Result r = addResultToSubmission(submission, assessmentType, user);
        r.setRated(rated);
        r.setScore(score);
        return resultRepo.save(r);
    }

    public Result addResultToSubmission(Submission submission, AssessmentType assessmentType, User user) {
        Result r = addResultToSubmission(submission);
        r.setAssessmentType(assessmentType);
        r.completionDate(ZonedDateTime.now());
        r.setAssessor(user);
        return resultRepo.save(r);
    }

    public Set<ExerciseHint> addHintsToExercise(Exercise exercise) {
        ExerciseHint exerciseHint1 = new ExerciseHint().exercise(exercise).title("title 1").content("content 1");
        ExerciseHint exerciseHint2 = new ExerciseHint().exercise(exercise).title("title 2").content("content 2");
        ExerciseHint exerciseHint3 = new ExerciseHint().exercise(exercise).title("title 3").content("content 3");
        Set<ExerciseHint> hints = new HashSet<>();
        hints.add(exerciseHint1);
        hints.add(exerciseHint2);
        hints.add(exerciseHint3);
        exercise.setExerciseHints(hints);
        exerciseHintRepository.saveAll(hints);

        return hints;
    }

    public ProgrammingExercise loadProgrammingExerciseWithEagerReferences() {
        final var lazyExercise = programmingExerciseRepository.findAll().get(0);
        return programmingExerciseTestRepository.findOneWithEagerEverything(lazyExercise);
    }

    public <T extends Exercise> T addHintsToProblemStatement(T exercise) {
        final var statement = exercise.getProblemStatement() == null ? "" : exercise.getProblemStatement();
        final var hintsInStatement = exercise.getExerciseHints().stream().map(ExerciseHint::getId).map(Object::toString).collect(Collectors.joining(", ", "{", "}"));
        exercise.setProblemStatement(statement + hintsInStatement);
        return exerciseRepo.save(exercise);
    }

    /**
     * Generates an example submission for a given model and exercise
     * @param model given uml model for the example submission
     * @param exercise exercise for which the example submission is created
     * @param flagAsExampleSubmission true if the submission is an example submission
     * @return  created example submission
     */
    public ExampleSubmission generateExampleSubmission(String model, Exercise exercise, boolean flagAsExampleSubmission) {
        return generateExampleSubmission(model, exercise, flagAsExampleSubmission, false);
    }

    /**
     * Generates an example submission for a given model and exercise
     * @param model given uml model for the example submission
     * @param exercise exercise for which the example submission is created
     * @param flagAsExampleSubmission true if the submission is an example submission
     * @param usedForTutorial true if the example submission is used for tutorial
     * @return  created example submission
     */
    public ExampleSubmission generateExampleSubmission(String model, Exercise exercise, boolean flagAsExampleSubmission, boolean usedForTutorial) {
        ModelingSubmission submission = ModelFactory.generateModelingSubmission(model, false);
        submission.setExampleSubmission(flagAsExampleSubmission);
        return ModelFactory.generateExampleSubmission(submission, exercise, usedForTutorial);
    }

    /**
     * Generates a submitted answer for a given question.
     * @param question given question, the answer is for
     * @param correct boolean whether the answer should be correct or not
     * @return created SubmittedAnswer
     */
    public SubmittedAnswer generateSubmittedAnswerFor(QuizQuestion question, boolean correct) {
        if (question instanceof MultipleChoiceQuestion) {
            var submittedAnswer = new MultipleChoiceSubmittedAnswer();
            submittedAnswer.setQuizQuestion(question);

            for (var answerOption : ((MultipleChoiceQuestion) question).getAnswerOptions()) {
                if (answerOption.isIsCorrect().equals(correct)) {
                    submittedAnswer.addSelectedOptions(answerOption);
                }
            }
            return submittedAnswer;
        }
        else if (question instanceof DragAndDropQuestion) {
            var submittedAnswer = new DragAndDropSubmittedAnswer();
            submittedAnswer.setQuizQuestion(question);

            DragItem dragItem1 = ((DragAndDropQuestion) question).getDragItems().get(0);
            dragItem1.setQuestion((DragAndDropQuestion) question);
            DragItem dragItem2 = ((DragAndDropQuestion) question).getDragItems().get(1);
            dragItem2.setQuestion((DragAndDropQuestion) question);

            DropLocation dropLocation1 = ((DragAndDropQuestion) question).getDropLocations().get(0);
            dropLocation1.setQuestion((DragAndDropQuestion) question);
            DropLocation dropLocation2 = ((DragAndDropQuestion) question).getDropLocations().get(1);
            dropLocation2.setQuestion((DragAndDropQuestion) question);

            if (correct) {
                submittedAnswer.addMappings(new DragAndDropMapping().dragItem(dragItem1).dropLocation(dropLocation1));
                submittedAnswer.addMappings(new DragAndDropMapping().dragItem(dragItem2).dropLocation(dropLocation2));
            }
            else {
                submittedAnswer.addMappings(new DragAndDropMapping().dragItem(dragItem2).dropLocation(dropLocation1));
                submittedAnswer.addMappings(new DragAndDropMapping().dragItem(dragItem1).dropLocation(dropLocation2));
            }

            return submittedAnswer;
        }
        else if (question instanceof ShortAnswerQuestion) {
            var submittedAnswer = new ShortAnswerSubmittedAnswer();
            submittedAnswer.setQuizQuestion(question);

            for (var spot : ((ShortAnswerQuestion) question).getSpots()) {
                ShortAnswerSubmittedText submittedText = new ShortAnswerSubmittedText();
                submittedText.setSpot(spot);
                if (correct) {
                    submittedText.setText(((ShortAnswerQuestion) question).getCorrectSolutionForSpot(spot).iterator().next().getText());
                }
                else {
                    submittedText.setText("wrong short answer");
                }
                submittedAnswer.addSubmittedTexts(submittedText);
            }
            return submittedAnswer;
        }
        return null;
    }

    @NotNull
    public QuizExercise createQuiz(Course course, ZonedDateTime releaseDate, ZonedDateTime dueDate) {
        QuizExercise quizExercise = ModelFactory.generateQuizExercise(releaseDate, dueDate, course);
        quizExercise.addQuestions(createMultipleChoiceQuestion());
        quizExercise.addQuestions(createDragAndDropQuestion());
        quizExercise.addQuestions(createShortAnswerQuestion());
        quizExercise.setMaxScore(quizExercise.getMaxTotalScore().doubleValue());
        quizExercise.setGradingInstructions(null);
        return quizExercise;
    }

    @NotNull
    public ShortAnswerQuestion createShortAnswerQuestion() {
        ShortAnswerQuestion sa = (ShortAnswerQuestion) new ShortAnswerQuestion().title("SA").score(2).text("This is a long answer text");
        sa.setScoringType(ScoringType.ALL_OR_NOTHING);
        var shortAnswerSpot1 = new ShortAnswerSpot().spotNr(0).width(1);
        shortAnswerSpot1.setTempID(generateTempId());
        var shortAnswerSpot2 = new ShortAnswerSpot().spotNr(2).width(2);
        shortAnswerSpot2.setTempID(generateTempId());
        sa.getSpots().add(shortAnswerSpot1);
        sa.getSpots().add(shortAnswerSpot2);
        var shortAnswerSolution1 = new ShortAnswerSolution().text("is");
        shortAnswerSolution1.setTempID(generateTempId());
        var shortAnswerSolution2 = new ShortAnswerSolution().text("long");
        shortAnswerSolution2.setTempID(generateTempId());
        sa.getSolutions().add(shortAnswerSolution1);
        sa.getSolutions().add(shortAnswerSolution2);
        sa.getCorrectMappings().add(new ShortAnswerMapping().spot(sa.getSpots().get(0)).solution(sa.getSolutions().get(0)));
        sa.getCorrectMappings().add(new ShortAnswerMapping().spot(sa.getSpots().get(1)).solution(sa.getSolutions().get(1)));
        sa.setExplanation("Explanation");
        return sa;
    }

    @NotNull
    public DragAndDropQuestion createDragAndDropQuestion() {
        DragAndDropQuestion dnd = (DragAndDropQuestion) new DragAndDropQuestion().title("DnD").score(3).text("Q2");
        dnd.setScoringType(ScoringType.PROPORTIONAL_WITH_PENALTY);
        var dropLocation1 = new DropLocation().posX(10d).posY(10d).height(10d).width(10d);
        dropLocation1.setTempID(generateTempId());
        var dropLocation2 = new DropLocation().posX(20d).posY(20d).height(10d).width(10d);
        dropLocation2.setTempID(generateTempId());
        dnd.getDropLocations().add(dropLocation1);
        dnd.getDropLocations().add(dropLocation2);
        var dragItem1 = new DragItem().text("D1");
        dragItem1.setTempID(generateTempId());
        var dragItem2 = new DragItem().text("D2");
        dragItem2.setTempID(generateTempId());
        dnd.getDragItems().add(dragItem1);
        dnd.getDragItems().add(dragItem2);
        dnd.getCorrectMappings().add(new DragAndDropMapping().dragItem(dragItem1).dropLocation(dropLocation1));
        dnd.getCorrectMappings().add(new DragAndDropMapping().dragItem(dragItem2).dropLocation(dropLocation2));
        dnd.setExplanation("Explanation");
        return dnd;
    }

    public Long generateTempId() {
        return ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);
    }

    @NotNull
    public MultipleChoiceQuestion createMultipleChoiceQuestion() {
        MultipleChoiceQuestion mc = (MultipleChoiceQuestion) new MultipleChoiceQuestion().title("MC").score(4).text("Q1");
        mc.setScoringType(ScoringType.ALL_OR_NOTHING);
        mc.getAnswerOptions().add(new AnswerOption().text("A").hint("H1").explanation("E1").isCorrect(true));
        mc.getAnswerOptions().add(new AnswerOption().text("B").hint("H2").explanation("E2").isCorrect(false));
        mc.setExplanation("Explanation");
        return mc;
    }

    /**
     * Generate submissions for a student for an exercise. Results are mixed.
     * @param quizExercise QuizExercise th submissions are for
     * @param studentID ID of the student
     * @param submitted Boolean if it is submitted or not
     * @param submissionDate Submission date
     */
    public QuizSubmission generateSubmission(QuizExercise quizExercise, int studentID, boolean submitted, ZonedDateTime submissionDate) {
        QuizSubmission quizSubmission = new QuizSubmission();
        QuizQuestion quizQuestion1 = quizExercise.getQuizQuestions().get(0);
        QuizQuestion quizQuestion2 = quizExercise.getQuizQuestions().get(1);
        QuizQuestion quizQuestion3 = quizExercise.getQuizQuestions().get(2);
        quizSubmission.addSubmittedAnswers(generateSubmittedAnswerFor(quizQuestion1, studentID % 2 == 0));
        quizSubmission.addSubmittedAnswers(generateSubmittedAnswerFor(quizQuestion2, studentID % 3 == 0));
        quizSubmission.addSubmittedAnswers(generateSubmittedAnswerFor(quizQuestion3, studentID % 4 == 0));
        quizSubmission.submitted(submitted);
        quizSubmission.submissionDate(submissionDate);

        return quizSubmission;
    }

    /**
     * Generate a submission with all or none options of a MultipleChoiceQuestion selected, if there is one in the exercise
     * @param quizExercise Exercise the submission is for
     * @param submitted Boolean whether it is submitted or not
     * @param submissionDate Submission date
     * @param selectEverything Boolean whether every answer option should be selected or none
     */
    public QuizSubmission generateSpecialSubmissionWithResult(QuizExercise quizExercise, boolean submitted, ZonedDateTime submissionDate, boolean selectEverything) {
        QuizSubmission quizSubmission = new QuizSubmission();

        for (QuizQuestion question : quizExercise.getQuizQuestions()) {
            if (question instanceof MultipleChoiceQuestion) {
                var submittedAnswer = new MultipleChoiceSubmittedAnswer();
                submittedAnswer.setQuizQuestion(question);
                if (selectEverything) {
                    for (var answerOption : ((MultipleChoiceQuestion) question).getAnswerOptions()) {
                        submittedAnswer.addSelectedOptions(answerOption);
                    }
                }
                quizSubmission.addSubmittedAnswers(submittedAnswer);

            }
            else {
                quizSubmission.addSubmittedAnswers(generateSubmittedAnswerFor(question, false));
            }
            quizSubmission.addSubmittedAnswers(generateSubmittedAnswerFor(question, false));
            quizSubmission.addSubmittedAnswers(generateSubmittedAnswerFor(question, false));
        }
        quizSubmission.submitted(submitted);
        quizSubmission.submissionDate(submissionDate);

        return quizSubmission;
    }

    // TODO: find some generic solution for the following duplicated code

    @NotNull
    public FileUploadExercise findFileUploadExerciseWithTitle(Collection<Exercise> exercises, String title) {
        Optional<Exercise> exercise = exercises.stream().filter(e -> e.getTitle().equals(title)).findFirst();
        if (exercise.isEmpty()) {
            fail("Could not find file upload exercise with title " + title);
        }
        else {
            if (exercise.get() instanceof FileUploadExercise) {
                return (FileUploadExercise) exercise.get();
            }
        }
        fail("Could not find file upload exercise with title " + title);
        // just to prevent compiler warnings, we have failed anyway here
        return new FileUploadExercise();
    }

    @NotNull
    public ModelingExercise findModelingExerciseWithTitle(Collection<Exercise> exercises, String title) {
        Optional<Exercise> exercise = exercises.stream().filter(e -> e.getTitle().equals(title)).findFirst();
        if (exercise.isEmpty()) {
            fail("Could not find modeling exercise with title " + title);
        }
        else {
            if (exercise.get() instanceof ModelingExercise) {
                return (ModelingExercise) exercise.get();
            }
        }
        fail("Could not find modeling exercise with title " + title);
        // just to prevent compiler warnings, we have failed anyway here
        return new ModelingExercise();
    }

    @NotNull
    public TextExercise findTextExerciseWithTitle(Collection<Exercise> exercises, String title) {
        Optional<Exercise> exercise = exercises.stream().filter(e -> e.getTitle().equals(title)).findFirst();
        if (exercise.isEmpty()) {
            fail("Could not find text exercise with title " + title);
        }
        else {
            if (exercise.get() instanceof TextExercise) {
                return (TextExercise) exercise.get();
            }
        }
        fail("Could not find text exercise with title " + title);
        // just to prevent compiler warnings, we have failed anyway here
        return new TextExercise();
    }

    @NotNull
    public ProgrammingExercise findProgrammingExerciseWithTitle(Collection<Exercise> exercises, String title) {
        Optional<Exercise> exercise = exercises.stream().filter(e -> e.getTitle().equals(title)).findFirst();
        if (exercise.isEmpty()) {
            fail("Could not find programming exercise with title " + title);
        }
        else {
            if (exercise.get() instanceof ProgrammingExercise) {
                return (ProgrammingExercise) exercise.get();
            }
        }
        fail("Could not find programming exercise with title " + title);
        // just to prevent compiler warnings, we have failed anyway here
        return new ProgrammingExercise();
    }
}
