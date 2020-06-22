import { ErrorHandler, Injectable } from '@angular/core';
import { captureException, init } from '@sentry/browser';
import { VERSION } from 'app/app.constants';
import { ProfileInfo } from 'app/shared/layouts/profiles/profile-info.model';

@Injectable({ providedIn: 'root' })
export class SentryErrorHandler extends ErrorHandler {
    private static get environment(): string {
        switch (window.location.host) {
            case 'artemis.ase.in.tum.de':
                return 'prod';
            case 'artemis.university4industry.com':
                return 'prod';
            case 'artemistest.ase.in.tum.de':
                return 'test';
            case 'artemistest2.ase.in.tum.de':
                return 'test';
            case 'vmbruegge60.in.tum.de':
                return 'apitests';
            default:
                return 'local';
        }
    }

    /**
     * Initialize Sentry with profile information.
     * @param profileInfo
     */
    public async initSentry(profileInfo: ProfileInfo): Promise<void> {
        if (!profileInfo || !profileInfo.sentry) {
            return;
        }

        init({
            dsn: profileInfo.sentry.dsn,
            release: VERSION,
            environment: SentryErrorHandler.environment,
        });
    }

    constructor() {
        super();
    }

    /**
     * Send an HttpError to Sentry. Only if it's not in the range 400-499.
     * @param error
     */
    handleError(error: any): void {
        if (error.name === 'HttpErrorResponse' && error.status < 500 && error.status >= 400) {
            super.handleError(error);
            return;
        }
        if (SentryErrorHandler.environment !== 'local') {
            captureException(error.originalError || error);
        }
        super.handleError(error);
    }
}
