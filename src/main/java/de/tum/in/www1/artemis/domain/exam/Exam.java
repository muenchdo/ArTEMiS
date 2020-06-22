package de.tum.in.www1.artemis.domain.exam;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.*;

import javax.persistence.*;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import de.tum.in.www1.artemis.domain.Course;
import de.tum.in.www1.artemis.domain.User;

@Entity
@Table(name = "exam")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Exam implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    /**
     * student can see the exam in the UI from {@link #visibleDate} date onwards
     */
    @Column(name = "visible_date")
    private ZonedDateTime visibleDate;

    /**
     * student can start working on exam from {@link #startDate}
     */
    @Column(name = "start_date")
    private ZonedDateTime startDate;

    /**
     * student can work on exam until {@link #endDate}
     */
    @Column(name = "end_date")
    private ZonedDateTime endDate;

    @Column(name = "start_text")
    @Lob
    private String startText;

    @Column(name = "end_text")
    @Lob
    private String endText;

    @Column(name = "confirmation_start_text")
    @Lob
    private String confirmationStartText;

    @Column(name = "confirmation_end_text")
    @Lob
    private String confirmationEndText;

    @Column(name = "max_points")
    private Integer maxPoints;

    @Column(name = "randomize_exercise_order")
    private Boolean randomizeExerciseOrder;

    /**
     * From all exercise groups connected to the exam, {@link #numberOfExercisesInExam} are randomly
     * chosen when generating the specific exam for the {@link #registeredUsers}
     */
    @Column(name = "number_of_exercises_in_exam")
    private Integer numberOfExercisesInExam;

    @ManyToOne
    @JoinColumn(name = "course_id")
    private Course course;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderColumn(name = "exercise_group_order")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JsonIgnoreProperties("exam")
    private List<ExerciseGroup> exerciseGroups = new ArrayList<>();

    // TODO: add a big fat warning in case instructor delete exams where student exams already exist
    @OneToMany(mappedBy = "exam", cascade = CascadeType.REMOVE, orphanRemoval = true, fetch = FetchType.LAZY)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JsonIgnoreProperties("exam")
    private Set<StudentExam> studentExams = new HashSet<>();

    // Unidirectional
    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @JoinTable(name = "exam_user", joinColumns = @JoinColumn(name = "exam_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "student_id", referencedColumnName = "id"))
    private Set<User> registeredUsers = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public ZonedDateTime getVisibleDate() {
        return visibleDate;
    }

    public void setVisibleDate(ZonedDateTime visibleDate) {
        this.visibleDate = visibleDate;
    }

    public ZonedDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(ZonedDateTime startDate) {
        this.startDate = startDate;
    }

    public ZonedDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(ZonedDateTime endDate) {
        this.endDate = endDate;
    }

    public String getStartText() {
        return startText;
    }

    public void setStartText(String startText) {
        this.startText = startText;
    }

    public String getEndText() {
        return endText;
    }

    public void setEndText(String endText) {
        this.endText = endText;
    }

    public String getConfirmationStartText() {
        return confirmationStartText;
    }

    public void setConfirmationStartText(String confirmationStartText) {
        this.confirmationStartText = confirmationStartText;
    }

    public String getConfirmationEndText() {
        return confirmationEndText;
    }

    public void setConfirmationEndText(String confirmationEndText) {
        this.confirmationEndText = confirmationEndText;
    }

    public Integer getMaxPoints() {
        return this.maxPoints;
    }

    public void setMaxPoints(Integer maxPoints) {
        this.maxPoints = maxPoints;
    }

    public Integer getNumberOfExercisesInExam() {
        return numberOfExercisesInExam;
    }

    public void setNumberOfExercisesInExam(Integer numberOfExercisesInExam) {
        this.numberOfExercisesInExam = numberOfExercisesInExam;
    }

    public Boolean getRandomizeExerciseOrder() {
        return randomizeExerciseOrder;
    }

    public void setRandomizeExerciseOrder(Boolean randomizeExerciseOrder) {
        this.randomizeExerciseOrder = randomizeExerciseOrder;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public List<ExerciseGroup> getExerciseGroups() {
        return exerciseGroups;
    }

    public void setExerciseGroups(List<ExerciseGroup> exerciseGroups) {
        this.exerciseGroups = exerciseGroups;
    }

    public Exam addExerciseGroup(ExerciseGroup exerciseGroup) {
        this.exerciseGroups.add(exerciseGroup);
        exerciseGroup.setExam(this);
        return this;
    }

    public Exam removeExerciseGroup(ExerciseGroup exerciseGroup) {
        this.exerciseGroups.remove(exerciseGroup);
        exerciseGroup.setExam(null);
        return this;
    }

    public Set<StudentExam> getStudentExams() {
        return studentExams;
    }

    public void setStudentExams(Set<StudentExam> studentExams) {
        this.studentExams = studentExams;
    }

    public Exam addStudentExam(StudentExam studentExam) {
        this.studentExams.add(studentExam);
        studentExam.setExam(this);
        return this;
    }

    public Exam removeStudentExam(StudentExam studentExam) {
        this.studentExams.remove(studentExam);
        studentExam.setExam(null);
        return this;
    }

    public Set<User> getRegisteredUsers() {
        return registeredUsers;
    }

    public void setRegisteredUsers(Set<User> registeredUsers) {
        this.registeredUsers = registeredUsers;
    }

    public Exam addUser(User user) {
        this.registeredUsers.add(user);
        return this;
    }

    public Exam removeUser(User user) {
        this.registeredUsers.remove(user);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Exam exam = (Exam) o;
        return Objects.equals(getId(), exam.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    /**
     * check if students are allowed to see this exam
     *
     * @return true, if students are allowed to see this exam, otherwise false
     */
    public Boolean isVisibleToStudents() {
        if (visibleDate == null) {  // no visible date means the exercise is visible to students
            return Boolean.TRUE;
        }
        return visibleDate.isBefore(ZonedDateTime.now());
    }
}
