import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { SERVER_API_URL } from 'app/app.constants';
import { ParticipationService } from 'app/exercises/shared/participation/participation.service';
import { WindowRef } from 'app/core/websocket/window.service';

@Injectable({ providedIn: 'root' })
export class SourceTreeService {
    constructor(private httpClient: HttpClient, private participationService: ParticipationService, private $window: WindowRef) {}

    /**
     * Build source tree url.
     * @param cloneUrl - url of the target.
     */
    buildSourceTreeUrl(cloneUrl: string): string {
        return 'sourcetree://cloneRepo?type=stash&cloneUrl=' + encodeURI(cloneUrl) + '&baseWebUrl=https://bitbucket.ase.in.tum.de';
    }

    /**
     * Return password of the repository.
     */
    getRepositoryPassword(): Observable<Object> {
        return this.httpClient.get(`${SERVER_API_URL}/api/account/password`);
    }
}
