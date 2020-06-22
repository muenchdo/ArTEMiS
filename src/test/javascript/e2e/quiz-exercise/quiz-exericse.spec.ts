import { NavBarPage, SignInPage } from '../page-objects/jhi-page-objects';
import { browser, by, element, ExpectedConditions as ec } from 'protractor';
import { CoursePage, NewCoursePage } from '../page-objects/entities/course-page-object';

const expect = chai.expect;

describe('quiz-exercise', function () {
    let navBarPage: NavBarPage;
    let signInPage: SignInPage;
    let coursePage: CoursePage;
    let newCoursePage: NewCoursePage;
    let courseId: string;
    let quizId: string;

    let courseName: string;

    before(async function () {
        await browser.get('/');
        navBarPage = new NavBarPage(true);
        signInPage = await navBarPage.getSignInPage();
        coursePage = new CoursePage();
        newCoursePage = new NewCoursePage();
        courseName = `Protractor${Date.now()}`;
        await signInPage.autoSignInUsing(process.env.bamboo_admin_user, process.env.bamboo_admin_password);

        await navBarPage.clickOnCourseAdminMenu();
        await coursePage.clickOnCreateNewCourse();

        await newCoursePage.setTitle(courseName);
        await newCoursePage.setShortName(courseName);
        await newCoursePage.setStudentGroupName('tumuser');
        await newCoursePage.setTutorGroupName('artemis-dev');
        await newCoursePage.setInstructorGroupName('ls1instructor');
        await newCoursePage.clickSave();

        await expect(browser.wait(ec.urlContains('/course'), 1000)).to.become(true);

        // Sign in with instructor account
        await navBarPage.autoSignOut();
        signInPage = await navBarPage.getSignInPage();
        await signInPage.autoSignInUsing(process.env.bamboo_instructor_user, process.env.bamboo_instructor_password);
    });

    beforeEach(async function () {});

    it('navigate into course-exercises', async function () {
        await navBarPage.clickOnCourseAdminMenu();
        courseId = await coursePage.navigateIntoLastCourseExercises();

        await expect(browser.wait(ec.urlContains(`/course/${courseId}`), 1000)).to.become(true);
    });

    it('create quiz', async function () {
        const createQuizButton = await element(by.id('create-quiz-button'));
        // expect(createQuizButton.isPresent());
        await createQuizButton.click();

        // set title of quiz
        const title = element(by.id('quiz-title'));
        title.sendKeys('test-quiz');

        // set duration of quiz
        const durationMinutes = await element(by.id('quiz-duration-minutes'));
        durationMinutes.clear();
        durationMinutes.sendKeys('0');
        const durationSeconds = await element(by.id('quiz-duration-seconds'));
        durationSeconds.clear();
        durationSeconds.sendKeys('5');

        // add MC question
        const addMcButton = await element(by.id('quiz-add-mc-question'));
        await addMcButton.click();

        // set title of mc question
        const mcQuestionTitle = await element(by.id('mc-question-title')); // TODO: we need to support multiple questions
        mcQuestionTitle.sendKeys('test-mc');

        // deactivate random order to make the test case deterministic
        const randomOrder = await element(by.css('[for="cbRandomizeOrderMC1"]'));
        await randomOrder.click();

        const quizSaveButton = await element(by.id('quiz-save'));
        expect(quizSaveButton.isPresent());
        await quizSaveButton.click();

        await expect(browser.wait(ec.urlContains(`${courseId}/quiz-exercise/new`), 1000)).to.become(true);

        const backButton = await element(by.id('quiz-cancel-back-button'));
        expect(backButton.isPresent());
        // TODO: check that the button name is "Back"
        await backButton.click();

        const quizRows = element.all(by.tagName('tbody')).all(by.tagName('tr'));
        quizId = await quizRows.last().element(by.css('td:nth-child(1) > a')).getText();

        // TODO: check that we leave the page and there is a new entry
    });

    it('participate in quiz', async function () {
        // set visible
        const setVisibleButton = await element(by.id(`quiz-set-visible-${quizId}`));
        expect(setVisibleButton.isPresent());
        await setVisibleButton.click();

        await browser.sleep(500); // let's wait shortly so that the server gets everything right with the database

        // start quiz
        const startQuizInstructorButton = await element(by.id(`instructor-quiz-start-${quizId}`));
        expect(startQuizInstructorButton.isPresent());
        await startQuizInstructorButton.click();

        await browser.sleep(500); // let's wait shortly so that the server gets everything right with the database
        // navigate to courses
        await navBarPage.clickOnOverviewMenu();

        browser.wait(ec.urlContains(`overview`), 1000).then((result: any) => expect(result).to.be.true);

        // open or start quiz (depends a bit on the timing)
        let startQuizButton = await element(by.id(`student-quiz-start-${quizId}`));
        if (!startQuizButton.isPresent()) {
            startQuizButton = await element(by.id(`student-quiz-open-${quizId}`));
        }
        expect(startQuizButton.isPresent());
        await startQuizButton.click();

        await expect(browser.wait(ec.urlContains(`quiz/${quizId}`), 1000)).to.become(true);

        // deactivate because we use timeouts in the quiz participation and otherwise it would not work
        browser.waitForAngularEnabled(false);

        await browser.sleep(2000); // wait till ui is loaded
        // answer quiz
        // TODO the answer options are random, search for the correct and incorrect answer option before clicking in it
        const firstAnswerOption = await element(by.id(`answer-option-0`));
        expect(firstAnswerOption.isPresent());
        await firstAnswerOption.click(); // select
        await firstAnswerOption.click(); // deselect
        await firstAnswerOption.click(); // select

        const secondAnswerOption = await element(by.id(`answer-option-1`));
        expect(secondAnswerOption.isPresent());
        await secondAnswerOption.click(); // select
        await secondAnswerOption.click(); // deselect

        // submit quiz
        const submitQuizButton = await element(by.id(`submit-quiz`));
        expect(submitQuizButton.isPresent());
        await submitQuizButton.click();

        // wait until the quiz has finished
        await expect(browser.wait(ec.visibilityOf(element(by.id('quiz-score'))), 15000)).to.become(true);

        await element(by.id('quiz-score-result'))
            .getText()
            .then((text) => {
                expect(text).equals('1/1 (100 %)');
            });

        await element(by.id('answer-option-0-correct'))
            .getText()
            .then((text) => {
                expect(text).equals('Correct');
            })
            .catch(() => {
                expect.fail('first answer option not found as correct');
            });

        await element(by.id('answer-option-1-wrong'))
            .getText()
            .then((text) => {
                expect(text).equals('Wrong');
            })
            .catch(() => {
                expect.fail('second answer option not found as correct');
            });

        browser.waitForAngularEnabled(true);
    });

    it('delete quiz', async function () {
        browser.waitForAngularEnabled(false);
        await browser.sleep(500); // let's wait shortly so that the server gets everything right with the database
        // navigate to course administration
        await navBarPage.clickOnCourseAdminMenu();

        browser.wait(ec.urlContains(`course`), 1000).then((result: any) => expect(result).to.be.true);
        browser.waitForAngularEnabled(true);
        courseId = await coursePage.navigateIntoLastCourseExercises();
        await element(by.id(`delete-quiz-${quizId}`)).click();
        await element(by.css('input[name="confirmExerciseName"]')).sendKeys('test-quiz');
        await element(by.id('delete-quiz-confirmation-button')).click();
    });

    it('create SA quiz', async function () {
        const createQuizButton = await element(by.id('create-quiz-button'));
        // expect(createQuizButton.isPresent());
        await createQuizButton.click();

        // set title of SA quiz
        const title = element(by.id('quiz-title'));
        title.sendKeys('test-SA-quiz');

        // set duration of quiz
        const durationMinutes = await element(by.id('quiz-duration-minutes'));
        durationMinutes.clear();
        durationMinutes.sendKeys('0');
        const durationSeconds = await element(by.id('quiz-duration-seconds'));
        durationSeconds.clear();
        durationSeconds.sendKeys('5');

        // add short answer question
        const addShortAnswerButton = await element(by.id('quiz-add-short-answer-question'));
        await addShortAnswerButton.click();

        // set title of short answer question
        const shortAnswerQuestionTitle = await element(by.id('short-answer-question-title')); // TODO: we need to support multiple questions
        shortAnswerQuestionTitle.sendKeys('test-short-answer');

        const quizSaveButton = await element(by.id('quiz-save'));
        expect(quizSaveButton.isPresent());
        await quizSaveButton.click();

        await expect(browser.wait(ec.urlContains(`${courseId}/quiz-exercise/new`), 1000)).to.become(true);

        const backButton = await element(by.id('quiz-cancel-back-button'));
        expect(backButton.isPresent());
        // TODO: check that the button name is "Back"
        await backButton.click();

        await browser
            .switchTo()
            .alert()
            .then((alert: any) => alert.accept())
            .catch(() => expect.fail('Did not show Alert on unsaved changes!'));

        // TODO: check that we leave the page and there is a new entry
    });

    after(async function () {
        await navBarPage.autoSignOut();
        signInPage = await navBarPage.getSignInPage();
        await signInPage.autoSignInUsing(process.env.bamboo_admin_user, process.env.bamboo_admin_password);

        await navBarPage.clickOnCourseAdminMenu();
        browser.waitForAngularEnabled(true);
        // Delete course

        let rows = element.all(by.tagName('tbody')).all(by.tagName('tr'));
        const numberOfCourses = await rows.count();
        const deleteButton = rows.last().element(by.className('btn-danger'));
        await deleteButton.click();

        const confirmDeleteButton = element(by.tagName('jhi-course-delete-dialog')).element(by.className('btn-danger'));
        await confirmDeleteButton.click();

        rows = element.all(by.tagName('tbody')).all(by.tagName('tr'));
        expect(await rows.count()).to.equal(numberOfCourses - 1);
        await navBarPage.autoSignOut();
    });
});
