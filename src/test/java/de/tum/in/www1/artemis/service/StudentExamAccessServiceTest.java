package de.tum.in.www1.artemis.service;

import static de.tum.in.www1.artemis.web.rest.util.ResponseUtil.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;

import de.tum.in.www1.artemis.AbstractSpringIntegrationBambooBitbucketJiraTest;
import de.tum.in.www1.artemis.domain.Course;
import de.tum.in.www1.artemis.domain.User;
import de.tum.in.www1.artemis.domain.exam.Exam;
import de.tum.in.www1.artemis.domain.exam.StudentExam;
import de.tum.in.www1.artemis.repository.CourseRepository;
import de.tum.in.www1.artemis.repository.ExamRepository;
import de.tum.in.www1.artemis.repository.StudentExamRepository;
import de.tum.in.www1.artemis.util.DatabaseUtilService;

public class StudentExamAccessServiceTest extends AbstractSpringIntegrationBambooBitbucketJiraTest {

    @Autowired
    DatabaseUtilService database;

    @Autowired
    StudentExamAccessService studentExamAccessService;

    @Autowired
    StudentExamRepository studentExamRepository;

    @Autowired
    ExamRepository examRepository;

    @Autowired
    CourseRepository courseRepository;

    private List<User> users;

    private Course course1;

    private Course course2;

    private Exam exam1;

    private Exam exam2;

    private StudentExam studentExam1;

    @BeforeEach
    void init() {
        users = database.addUsers(2, 0, 0);
        course1 = database.addEmptyCourse();
        course2 = database.addEmptyCourse();
        course2.setStudentGroupName("another-group");
        courseRepository.save(course2);
        exam1 = database.addActiveExamWithRegisteredUser(course1, users.get(0));
        studentExam1 = database.addStudentExam(exam1);
        studentExam1.setUser(users.get(0));
        studentExamRepository.save(studentExam1);
        exam2 = database.addExam(course2);
    }

    @AfterEach
    public void tearDown() {
        database.resetDatabase();
    }

    @Test
    @WithMockUser(username = "student1", roles = "USER")
    public void testIsAtLeastStudentInCourse() {
        Optional<ResponseEntity<Void>> accessFailure1 = studentExamAccessService.checkCourseAndExamAccess(course2.getId(), exam2.getId(), users.get(0));
        assertThat(accessFailure1.isPresent()).isTrue();
        assertThat(accessFailure1.get()).isEqualTo(forbidden());
        Optional<ResponseEntity<Void>> accessFailure2 = studentExamAccessService.checkStudentExamAccess(course2.getId(), exam2.getId(), studentExam1.getId());
        assertThat(accessFailure2.isPresent()).isTrue();
        assertThat(accessFailure2.get()).isEqualTo(forbidden());
    }

    @Test
    @WithMockUser(username = "student1", roles = "USER")
    public void testExamExists() {
        Optional<ResponseEntity<Void>> accessFailure1 = studentExamAccessService.checkCourseAndExamAccess(course1.getId(), 55L, users.get(0));
        assertThat(accessFailure1.isPresent()).isTrue();
        assertThat(accessFailure1.get()).isEqualTo(notFound());
        Optional<ResponseEntity<Void>> accessFailure2 = studentExamAccessService.checkStudentExamAccess(course1.getId(), 55L, studentExam1.getId());
        assertThat(accessFailure2.isPresent()).isTrue();
        assertThat(accessFailure2.get()).isEqualTo(notFound());
    }

    @Test
    @WithMockUser(username = "student1", roles = "USER")
    public void testExamBelongsToCourse() {
        Optional<ResponseEntity<Void>> accessFailure1 = studentExamAccessService.checkCourseAndExamAccess(course1.getId(), exam2.getId(), users.get(0));
        assertThat(accessFailure1.isPresent()).isTrue();
        assertThat(accessFailure1.get()).isEqualTo(conflict());
        Optional<ResponseEntity<Void>> accessFailure2 = studentExamAccessService.checkStudentExamAccess(course1.getId(), exam2.getId(), studentExam1.getId());
        assertThat(accessFailure2.isPresent()).isTrue();
        assertThat(accessFailure2.get()).isEqualTo(conflict());
    }

    @Test
    @WithMockUser(username = "student1", roles = "USER")
    public void testExamIsLive() {
        // Exam has not started.
        Exam examNotStarted = database.addExam(course1, users.get(0), ZonedDateTime.now().plusHours(1), ZonedDateTime.now().plusHours(3));
        Optional<ResponseEntity<Void>> accessFailure1_1 = studentExamAccessService.checkCourseAndExamAccess(course1.getId(), examNotStarted.getId(), users.get(0));
        assertThat(accessFailure1_1.isPresent()).isTrue();
        assertThat(accessFailure1_1.get()).isEqualTo(forbidden());
        Optional<ResponseEntity<Void>> accessFailure1_2 = studentExamAccessService.checkStudentExamAccess(course1.getId(), examNotStarted.getId(), studentExam1.getId());
        assertThat(accessFailure1_2.isPresent()).isTrue();
        assertThat(accessFailure1_2.get()).isEqualTo(forbidden());
        // Exam has ended.
        Exam examEnded = database.addExam(course1, users.get(0), ZonedDateTime.now().minusHours(3), ZonedDateTime.now().minusHours(1));
        Optional<ResponseEntity<Void>> accessFailure2_1 = studentExamAccessService.checkCourseAndExamAccess(course1.getId(), examEnded.getId(), users.get(0));
        assertThat(accessFailure2_1.isPresent()).isTrue();
        assertThat(accessFailure2_1.get()).isEqualTo(forbidden());
        Optional<ResponseEntity<Void>> accessFailure2_2 = studentExamAccessService.checkStudentExamAccess(course1.getId(), examEnded.getId(), studentExam1.getId());
        assertThat(accessFailure2_2.isPresent()).isTrue();
        assertThat(accessFailure2_2.get()).isEqualTo(forbidden());
    }

    @Test
    @WithMockUser(username = "student1", roles = "USER")
    public void testUserIsRegisteredForExam() {
        Exam examNotRegistered = database.addExam(course1, users.get(1), ZonedDateTime.now().minusHours(1), ZonedDateTime.now().plusHours(1));
        Optional<ResponseEntity<Void>> accessFailure1 = studentExamAccessService.checkCourseAndExamAccess(course1.getId(), examNotRegistered.getId(), users.get(0));
        assertThat(accessFailure1.isPresent()).isTrue();
        assertThat(accessFailure1.get()).isEqualTo(forbidden());
        Optional<ResponseEntity<Void>> accessFailure2 = studentExamAccessService.checkStudentExamAccess(course1.getId(), examNotRegistered.getId(), studentExam1.getId());
        assertThat(accessFailure2.isPresent()).isTrue();
        assertThat(accessFailure2.get()).isEqualTo(forbidden());
    }

    @Test
    @WithMockUser(username = "student1", roles = "USER")
    public void testUserStudentExamExists() {
        Optional<ResponseEntity<Void>> accessFailure = studentExamAccessService.checkStudentExamAccess(course1.getId(), exam1.getId(), 55L);
        assertThat(accessFailure.isPresent()).isTrue();
        assertThat(accessFailure.get()).isEqualTo(notFound());
    }

    @Test
    @WithMockUser(username = "student1", roles = "USER")
    public void testExamIdEqualsExamOfStudentExam() {
        StudentExam studentExamNotRelatedToExam1 = database.addStudentExam(exam2);
        Optional<ResponseEntity<Void>> accessFailure = studentExamAccessService.checkStudentExamAccess(course1.getId(), exam1.getId(), studentExamNotRelatedToExam1.getId());
        assertThat(accessFailure.isPresent()).isTrue();
        assertThat(accessFailure.get()).isEqualTo(conflict());
    }

    @Test
    @WithMockUser(username = "student1", roles = "USER")
    public void testCurrentUserIsUserOfStudentExam() {
        StudentExam studentExamWithOtherUser = database.addStudentExam(exam1);
        studentExamWithOtherUser.setUser(users.get(1));
        studentExamRepository.save(studentExamWithOtherUser);
        Optional<ResponseEntity<Void>> accessFailure = studentExamAccessService.checkStudentExamAccess(course1.getId(), exam1.getId(), studentExamWithOtherUser.getId());
        assertThat(accessFailure.isPresent()).isTrue();
        assertThat(accessFailure.get()).isEqualTo(forbidden());
    }
}
