import { Component, OnDestroy } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { JhiAlert, JhiEventManager, JhiEventWithContent } from 'ng-jhipster';
import { Subscription } from 'rxjs/Subscription';
import { AlertService } from 'app/core/alert/alert.service';
import { AlertError } from 'app/shared/alert/alert-error.model';

@Component({
    selector: 'jhi-alert-error',
    template: `
        <div class="alerts" role="alert">
            <div *ngFor="let alert of alerts" [ngClass]="setClasses(alert)">
                <ngb-alert *ngIf="alert && alert.type && alert.msg" [type]="alert.type" (close)="alert.close(alerts)">
                    <pre [innerHTML]="alert.msg"></pre>
                </ngb-alert>
            </div>
        </div>
    `,
})
export class AlertErrorComponent implements OnDestroy {
    alerts: JhiAlert[] = [];
    errorListener: Subscription;
    httpErrorListener: Subscription;

    constructor(private alertService: AlertService, private eventManager: JhiEventManager, private translateService: TranslateService) {
        this.errorListener = eventManager.subscribe('artemisApp.error', (response: JhiEventWithContent<AlertError>) => {
            const errorResponse = response.content;
            this.addErrorAlert(errorResponse.message, errorResponse.key, errorResponse.params);
        });

        this.httpErrorListener = eventManager.subscribe('artemisApp.httpError', (response: any) => {
            const httpErrorResponse = response.content;
            switch (httpErrorResponse.status) {
                // connection refused, server not reachable
                case 0:
                    this.addErrorAlert('Server not reachable', 'error.server.not.reachable');
                    break;

                case 400:
                    if (httpErrorResponse.error !== null && httpErrorResponse.error.skipAlert) {
                        break;
                    }
                    const arr = httpErrorResponse.headers.keys();
                    let errorHeader = null;
                    let entityKey = null;
                    arr.forEach((entry: string) => {
                        if (entry.toLowerCase().endsWith('app-error')) {
                            errorHeader = httpErrorResponse.headers.get(entry);
                        } else if (entry.toLowerCase().endsWith('app-params')) {
                            entityKey = httpErrorResponse.headers.get(entry);
                        }
                    });
                    if (errorHeader) {
                        const entityName = translateService.instant('global.menu.entities.' + entityKey);
                        this.addErrorAlert(errorHeader, errorHeader, { entityName });
                    } else if (httpErrorResponse.error !== '' && httpErrorResponse.error.fieldErrors) {
                        const fieldErrors = httpErrorResponse.error.fieldErrors;
                        for (const fieldError of fieldErrors) {
                            if (['Min', 'Max', 'DecimalMin', 'DecimalMax'].includes(fieldError.message)) {
                                fieldError.message = 'Size';
                            }
                            // convert 'something[14].other[4].id' to 'something[].other[].id' so translations can be written to it
                            const convertedField = fieldError.field.replace(/\[\d*\]/g, '[]');
                            const fieldName = translateService.instant('artemisApp.' + fieldError.objectName + '.' + convertedField);
                            this.addErrorAlert('Error on field "' + fieldName + '"', 'error.' + fieldError.message, { fieldName });
                        }
                    } else if (httpErrorResponse.error !== '' && httpErrorResponse.error.message) {
                        this.addErrorAlert(httpErrorResponse.error.message, httpErrorResponse.error.message, httpErrorResponse.error.params);
                    } else {
                        this.addErrorAlert(httpErrorResponse.error);
                    }
                    break;

                case 404:
                    this.addErrorAlert('Not found', 'error.url.not.found');
                    break;

                default:
                    if (httpErrorResponse.error && httpErrorResponse.error.message) {
                        this.addErrorAlert(httpErrorResponse.error.message);
                    } else {
                        this.addErrorAlert(httpErrorResponse.error);
                    }
            }
        });
    }

    /**
     * set classes for alert
     * @param {JhiAlert} alert
     * @return {{ [key: string]: boolean }}
     */
    setClasses(alert: JhiAlert): { [key: string]: boolean } {
        const classes = { 'jhi-toast': Boolean(alert.toast) };
        if (alert.position) {
            return { ...classes, [alert.position]: true };
        }
        return classes;
    }

    /**
     * remove listeners on destroy
     */
    ngOnDestroy(): void {
        if (this.errorListener) {
            this.eventManager.destroy(this.errorListener);
        }
        if (this.httpErrorListener) {
            this.eventManager.destroy(this.httpErrorListener);
        }
    }

    /**
     * add a new alert
     * @param {string} message
     * @param {string} key?
     * @param {any} data?
     */
    addErrorAlert(message: string, key?: string, data?: any): void {
        message = key && key !== null ? key : message;

        const newAlert: JhiAlert = {
            type: 'danger',
            msg: message,
            params: data,
            timeout: 5000,
            toast: this.alertService.isToast(),
            scoped: true,
        };

        this.alerts.push(this.alertService.addAlert(newAlert, this.alerts));
    }
}
