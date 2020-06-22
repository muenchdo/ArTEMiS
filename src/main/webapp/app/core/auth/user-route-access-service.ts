import { Injectable, isDevMode } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from '@angular/router';
import { LocalStorageService } from 'ngx-webstorage';
import { AccountService } from 'app/core/auth/account.service';
import { StateStorageService } from 'app/core/auth/state-storage.service';
import { OrionVersionValidator } from 'app/shared/orion/outdated-plugin-warning/orion-version-validator.service';
import { first, switchMap } from 'rxjs/operators';
import { from, of } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class UserRouteAccessService implements CanActivate {
    constructor(
        private router: Router,
        private accountService: AccountService,
        private stateStorageService: StateStorageService,
        private localStorage: LocalStorageService,
        private orionVersionValidator: OrionVersionValidator,
    ) {}

    /**
     * Check if the client can activate a route.
     * @param route {ActivatedRouteSnapshot} The ActivatedRouteSnapshot of the route to activate.
     * @param state {RouterStateSnapshot} The current RouterStateSnapshot.
     * @return {(boolean | Promise<boolean>)} True if Orion version is valid or the connected client is a regular browser and
     * user is logged in, false otherwise.
     */
    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean | Promise<boolean> {
        // save the jwt token from get parameter for lti launch requests for online course users
        // Note: The following URL has to match the redirect URL in LtiResource.java in the method launch(...) shortly before the return
        if (route.routeConfig!.path === 'courses/:courseId/exercises/:exerciseId' && route.queryParams['jwt']) {
            const jwt = route.queryParams['jwt'];
            this.localStorage.store('authenticationToken', jwt);
        }

        const authorities = route.data['authorities'];
        // We need to call the checkLogin / and so the accountService.identity() function, to ensure,
        // that the client has an account too, if they already logged in by the server.
        // This could happen on a page refresh.
        return (
            this.orionVersionValidator
                // Returns true, if the Orion version is up-to-date, or the connected client is just a regular browser
                .validateOrionVersion()
                .pipe(
                    // Only take the first returned boolean and then cancel the subscription
                    first(),
                    switchMap((isValidOrNoIDE) => {
                        // 1./2. Case: The Orion version is up-to-date/The connected client is a regular browser
                        if (isValidOrNoIDE) {
                            // Always check whether the user is logged in
                            return from(this.checkLogin(authorities, state.url));
                        }
                        // 3. Case: The Orion Version is not up-to-date
                        return of(false);
                    }),
                )
                .toPromise()
        );
    }

    /**
     * Check whether user is logged in and has the required authorities.
     * @param {string[]}authorities List of required authorities.
     * @param {string} url Current url.
     * @return {Promise<boolean>} True if authorities are empty or null, False if user not logged in or does not have required authorities.
     */
    checkLogin(authorities: string[], url: string): Promise<boolean> {
        const accountService = this.accountService;
        return Promise.resolve(
            accountService.identity().then((account) => {
                if (!authorities || authorities.length === 0) {
                    return true;
                }

                if (account) {
                    return accountService.hasAnyAuthority(authorities).then((response) => {
                        if (response) {
                            return true;
                        }
                        if (isDevMode()) {
                            console.error('User has not any of required authorities: ', authorities);
                        }
                        return false;
                    });
                }

                this.stateStorageService.storeUrl(url);
                this.router.navigate(['accessdenied']).then(() => {
                    // only show the login dialog, if the user hasn't logged in yet
                    if (!account) {
                        this.router.navigate(['/']);
                    }
                });
                return false;
            }),
        );
    }
}
