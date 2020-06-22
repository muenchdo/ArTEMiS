import { Component, OnDestroy, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { Subscription } from 'rxjs/Subscription';
import { SystemNotification } from 'app/entities/system-notification.model';
import { SystemNotificationService } from 'app/shared/notification/system-notification/system-notification.service';

@Component({
    selector: 'jhi-system-notification-management-detail',
    templateUrl: './system-notification-management-detail.component.html',
})
export class SystemNotificationManagementDetailComponent implements OnInit, OnDestroy {
    notification: SystemNotification;
    private subscription: Subscription;

    constructor(private systemNotificationService: SystemNotificationService, private route: ActivatedRoute) {}

    /**
     * Assigns the subscription to system notification service
     */
    ngOnInit() {
        this.subscription = this.route.params.subscribe((params) => {
            this.load(params['id']);
        });
    }

    /**
     * Loads system notification
     * @param id of the system notification
     */
    load(id: string) {
        this.systemNotificationService.find(parseInt(id, 10)).subscribe((response) => {
            this.notification = response.body!;
        });
    }

    /**
     * Unsubscribe on component destruction
     */
    ngOnDestroy() {
        this.subscription.unsubscribe();
    }
}
