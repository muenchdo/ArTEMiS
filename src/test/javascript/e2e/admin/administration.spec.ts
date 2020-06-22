import { browser, by, element } from 'protractor';

import { NavBarPage, SignInPage } from '../page-objects/jhi-page-objects';

const expect = chai.expect;

describe('administration', function () {
    let navBarPage: NavBarPage;
    let signInPage: SignInPage;

    before(async function () {
        await browser.get('/');
        navBarPage = new NavBarPage(true);
        signInPage = await navBarPage.getSignInPage();
        await signInPage.autoSignInUsing(process.env.bamboo_admin_user, process.env.bamboo_admin_password);
    });

    beforeEach(async function () {
        await navBarPage.clickOnAdminMenu();
    });

    it('should load user management', async function () {
        await navBarPage.clickOnAdmin('user-management');
        const expect1 = 'userManagement.home.title';
        const value1 = await element(by.id('user-management-page-heading')).getAttribute('jhiTranslate');
        expect(value1).to.eq(expect1);
    });

    it('should load metrics', async function () {
        await navBarPage.clickOnAdmin('jhi-metrics');
        const expect1 = 'metrics.title';
        const value1 = await element(by.id('metrics-page-heading')).getAttribute('jhiTranslate');
        expect(value1).to.eq(expect1);
    });

    it('should load health', async function () {
        await navBarPage.clickOnAdmin('jhi-health');
        const expect1 = 'health.title';
        const value1 = await element(by.id('health-page-heading')).getAttribute('jhiTranslate');
        expect(value1).to.eq(expect1);
    });

    it('should load configuration', async function () {
        await navBarPage.clickOnAdmin('jhi-configuration');
        const expect1 = 'configuration.title';
        const value1 = await element(by.id('configuration-page-heading')).getAttribute('jhiTranslate');
        expect(value1).to.eq(expect1);
    });

    it('should load audits', async function () {
        await navBarPage.clickOnAdmin('audits');
        const expect1 = 'audits.title';
        const value1 = await element(by.id('audits-page-heading')).getAttribute('jhiTranslate');
        expect(value1).to.eq(expect1);
    });

    it('should load logs', async function () {
        await navBarPage.clickOnAdmin('logs');
        const expect1 = 'logs.title';
        const value1 = await element(by.id('logs-page-heading')).getAttribute('jhiTranslate');
        expect(value1).to.eq(expect1);
    });

    after(async function () {
        await navBarPage.autoSignOut();
    });
});
