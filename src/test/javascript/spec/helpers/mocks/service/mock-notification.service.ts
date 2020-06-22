import { BehaviorSubject, Observable, of } from 'rxjs';
import { HttpResponse } from '@angular/common/http';
import { Notification } from 'app/entities/notification.model';
import { GroupNotification } from 'app/entities/group-notification.model';

export class MockNotificationService {
    query = (req?: any): Observable<HttpResponse<Notification[]>> => of();
    subscribeUserNotifications = (): Promise<any> => Promise.resolve();
    subscribeToSocketMessages = (): BehaviorSubject<Notification | null> => new BehaviorSubject(null);
    interpretNotification = (notification: GroupNotification): void => {};
    cleanUp = () => {};
}
