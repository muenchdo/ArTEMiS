exports.config = {
    allScriptsTimeout: 20000,

    specs: [
        './src/test/javascript/e2e/**/*.spec.ts',
        /* jhipster-needle-add-protractor-tests - JHipster will add protractors tests here */
    ],

    capabilities: {
        browserName: 'chrome',
        chromeOptions: {
            args: process.env.JHI_E2E_HEADLESS
                ? ['--headless', '--disable-gpu', '--window-size=1280,1024', '--disable-extensions', 'incognito']
                : ['--disable-gpu', '--window-size=1280,1024', '--disable-extensions', 'incognito'],
        },
    },

    directConnect: true,

    baseUrl: 'http://localhost:8080/',

    framework: 'mocha',

    SELENIUM_PROMISE_MANAGER: false,

    mochaOpts: {
        reporter: 'spec',
        slow: 3000,
        ui: 'bdd',
        timeout: 720000,
    },

    beforeLaunch: function () {
        require('ts-node').register({
            project: 'tsconfig.e2e.json',
        });
    },

    onPrepare: function () {
        browser.driver.manage().window().setRect({ x: 100, y: 100, width: 1280, height: 1024 });
        // Disable animations
        // @ts-ignore
        browser.executeScript('document.body.className += " notransition";');
        var chai = require('chai');
        var chaiAsPromised = require('chai-as-promised');
        chai.use(chaiAsPromised);
        var chaiString = require('chai-string');
        chai.use(chaiString);
        // @ts-ignore
        global.chai = chai;
    },

    useAllAngular2AppRoots: true,
};
