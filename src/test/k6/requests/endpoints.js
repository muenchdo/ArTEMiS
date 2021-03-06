export const PROGRAMMING_EXERCISES_SETUP = '/programming-exercises/setup';
export const PROGRAMMING_EXERCISES = '/programming-exercises';
export const PROGRAMMING_EXERCISE = (exerciseId) => `${PROGRAMMING_EXERCISES}/${exerciseId}`;
export const QUIZ_EXERCISES = '/quiz-exercises';
export const QUIZ_EXERCISE = (exerciseId) => `${QUIZ_EXERCISES}/${exerciseId}`;
export const COURSES = '/courses';
export const USERS = '/users';
export const COURSE = (courseId) => `${COURSES}/${courseId}`;
export const COURSE_STUDENTS = (courseId, username) => `${COURSES}/${courseId}/students/${username}`;
export const COURSE_TUTORS = (courseId, username) => `${COURSES}/${courseId}/tutors/${username}`;
export const COURSE_INSTRUCTORS = (courseId, username) => `${COURSES}/${courseId}/instructors/${username}`;
export const EXERCISES = (courseId) => `${COURSE(courseId)}/exercises`;
export const EXERCISE = (courseId, exerciseId) => `${EXERCISES(courseId)}/${exerciseId}`;
export const PARTICIPATION = (exerciseId) => `/exercises/${exerciseId}/participation`;
export const PARTICIPATIONS = (courseId, exerciseId) => `${EXERCISE(courseId, exerciseId)}/participations`;
export const COMMIT = (participationId) => `/repository/${participationId}/commit`;
export const NEW_FILE = (participationId) => `/repository/${participationId}/file`;
export const PARTICIPATION_WITH_RESULT = (participationId) => `/participations/${participationId}/withLatestResult`;
export const SUBMIT_QUIZ_LIVE = (exerciseId) => `/exercises/${exerciseId}/submissions/live`;
