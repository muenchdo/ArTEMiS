import { CodeEditorContainer } from 'app/exercises/programming/shared/code-editor/code-editor-mode-container.component';
import { OnDestroy, OnInit } from '@angular/core';
import { Observable, Subscription, throwError } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';
import { CourseExerciseService } from '../../../../course/manage/course-management.service';
import { TranslateService } from '@ngx-translate/core';
import { AlertService } from 'app/core/alert/alert.service';
import { catchError, filter, map, tap } from 'rxjs/operators';
import { ParticipationService } from 'app/exercises/shared/participation/participation.service';
import { Participation } from 'app/entities/participation/participation.model';
import { ButtonSize } from 'app/shared/components/button.component';
import { CodeEditorSessionService } from 'app/exercises/programming/shared/code-editor/service/code-editor-session.service';
import { DomainService } from 'app/exercises/programming/shared/code-editor/service/code-editor-domain.service';
import { CodeEditorFileService } from 'app/exercises/programming/shared/code-editor/service/code-editor-file.service';
import { TemplateProgrammingExerciseParticipation } from 'app/entities/participation/template-programming-exercise-participation.model';
import { ProgrammingExerciseParticipationService } from 'app/exercises/programming/manage/services/programming-exercise-participation.service';
import { ExerciseType } from 'app/entities/exercise.model';
import { ProgrammingExerciseService } from 'app/exercises/programming/manage/services/programming-exercise.service';
import { ProgrammingExercise } from 'app/entities/programming-exercise.model';
import { ProgrammingExerciseStudentParticipation } from 'app/entities/participation/programming-exercise-student-participation.model';
import { SolutionProgrammingExerciseParticipation } from 'app/entities/participation/solution-programming-exercise-participation.model';
import { DomainChange, DomainType } from 'app/exercises/programming/shared/code-editor/model/code-editor.model';

/**
 * Enumeration specifying the repository type
 */
export enum REPOSITORY {
    ASSIGNMENT = 'ASSIGNMENT',
    TEMPLATE = 'TEMPLATE',
    SOLUTION = 'SOLUTION',
    TEST = 'TEST',
}

/**
 * Enumeration specifying the loading state
 */
export enum LOADING_STATE {
    CLEAR = 'CLEAR',
    INITIALIZING = 'INITIALIZING',
    FETCHING_FAILED = 'FETCHING_FAILED',
    CREATING_ASSIGNMENT_REPO = 'CREATING_ASSIGNMENT_REPO',
    DELETING_ASSIGNMENT_REPO = 'DELETING_ASSIGNMENT_REPO',
}

export abstract class CodeEditorInstructorBaseContainerComponent extends CodeEditorContainer implements OnInit, OnDestroy {
    ButtonSize = ButtonSize;
    REPOSITORY = REPOSITORY;
    LOADING_STATE = LOADING_STATE;
    PROGRAMMING = ExerciseType.PROGRAMMING;

    // This component responds to multiple route schemas:
    // :exerciseId -> Load exercise and select template repository
    // :exerciseId/:participationId -> Load exercise and select repository according to participationId
    // :exerciseId/test -> Load exercise and select test repository
    paramSub: Subscription;

    // Contains all participations (template, solution, assignment)
    exercise: ProgrammingExercise;
    // Can only be null when the test repository is selected.
    selectedParticipation: TemplateProgrammingExerciseParticipation | SolutionProgrammingExerciseParticipation | ProgrammingExerciseStudentParticipation | null;
    // Stores which repository is selected atm.
    // Needs to be set additionally to selectedParticipation as the test repository does not have a participation
    selectedRepository: REPOSITORY;

    // Fires when the selected domain changes.
    // This can either be a participation (solution, template, assignment) or the test repository.
    domainChangeSubscription: Subscription;

    // State variables
    loadingState = LOADING_STATE.CLEAR;

    protected constructor(
        protected router: Router,
        private exerciseService: ProgrammingExerciseService,
        private courseExerciseService: CourseExerciseService,
        private domainService: DomainService,
        private programmingExerciseParticipationService: ProgrammingExerciseParticipationService,
        participationService: ParticipationService,
        translateService: TranslateService,
        route: ActivatedRoute,
        jhiAlertService: AlertService,
        sessionService: CodeEditorSessionService,
        fileService: CodeEditorFileService,
    ) {
        super(participationService, translateService, route, jhiAlertService, sessionService, fileService);
    }

    /**
     * Initialize the route params subscription.
     * On route param change load the exercise and the selected participation OR the test repository.
     */
    ngOnInit(): void {
        if (this.paramSub) {
            this.paramSub.unsubscribe();
        }
        this.paramSub = this.route.params.subscribe((params) => {
            const exerciseId = Number(params['exerciseId']);
            const participationId = Number(params['participationId']);
            this.loadingState = LOADING_STATE.INITIALIZING;
            this.loadExercise(exerciseId)
                .pipe(
                    catchError(() => throwError('exerciseNotFound')),
                    tap((exercise) => (this.exercise = exercise)),
                    // Set selected participation
                    tap(() => {
                        if (this.router.url.endsWith('/test')) {
                            this.domainService.setDomain([DomainType.TEST_REPOSITORY, this.exercise]);
                        } else {
                            const nextAvailableParticipation = this.getNextAvailableParticipation(participationId);
                            if (nextAvailableParticipation) {
                                this.selectParticipationDomainById(nextAvailableParticipation.id);
                            } else {
                                throwError('participationNotFound');
                            }
                        }
                    }),
                    tap(() => {
                        if (!this.domainChangeSubscription) {
                            this.domainChangeSubscription = this.subscribeToDomainChange();
                        }
                    }),
                )
                .subscribe(
                    () => (this.loadingState = LOADING_STATE.CLEAR),
                    (err) => {
                        this.loadingState = LOADING_STATE.FETCHING_FAILED;
                        this.onError(err);
                    },
                );
        });
    }

    /**
     * Unsubscribe from paramSub and domainChangeSubscription if they are present, on component destruction
     */
    ngOnDestroy() {
        if (this.domainChangeSubscription) {
            this.domainChangeSubscription.unsubscribe();
        }
        if (this.paramSub) {
            this.paramSub.unsubscribe();
        }
    }

    /**
     * Get the next available participation, highest priority has the participation given to the method.
     * Removes participations without a repositoryUrl (could be invalid).
     * Returns undefined if no valid participation can be found.
     *
     * @param preferredParticipationId
     */
    private getNextAvailableParticipation(preferredParticipationId: number): Participation | undefined {
        const availableParticipations = [
            this.exercise.templateParticipation,
            this.exercise.solutionParticipation,
            this.exercise.studentParticipations && this.exercise.studentParticipations.length ? this.exercise.studentParticipations[0] : undefined,
        ].filter(Boolean);
        const selectedParticipation = availableParticipations.find(({ id }: ProgrammingExerciseStudentParticipation) => id === preferredParticipationId);
        return [selectedParticipation, ...availableParticipations].filter(Boolean).find(({ repositoryUrl }: ProgrammingExerciseStudentParticipation) => !!repositoryUrl);
    }

    /**
     * Subscribe for domain changes caused by url route changes.
     * Distinguishes between participation based domains (template, solution, assignment) and the test repository.
     */
    subscribeToDomainChange() {
        return this.domainService
            .subscribeDomainChange()
            .pipe(
                filter((domain) => !!domain),
                map((domain) => domain as DomainChange),
                tap(([domainType, domainValue]) => {
                    this.applyDomainChange(domainType, domainValue);
                }),
            )
            .subscribe();
    }

    protected applyDomainChange(domainType: any, domainValue: any) {
        this.initializeProperties();
        if (domainType === DomainType.PARTICIPATION) {
            this.setSelectedParticipation(domainValue.id);
        } else {
            this.selectedParticipation = null;
            this.selectedRepository = REPOSITORY.TEST;
        }
    }

    /**
     * Set the selected participation based on a its id.
     * Shows an error if the participationId does not match the template, solution or assignment participation.
     **/
    setSelectedParticipation(participationId: number) {
        // The result component needs a circular structure of participation -> exercise.
        const exercise = this.exercise;
        if (participationId === this.exercise.templateParticipation.id) {
            this.selectedRepository = REPOSITORY.TEMPLATE;
            this.selectedParticipation = this.exercise.templateParticipation;
            this.selectedParticipation.programmingExercise = exercise;
        } else if (participationId === this.exercise.solutionParticipation.id) {
            this.selectedRepository = REPOSITORY.SOLUTION;
            this.selectedParticipation = this.exercise.solutionParticipation;
            this.selectedParticipation.programmingExercise = exercise;
        } else if (this.exercise.studentParticipations.length && participationId === this.exercise.studentParticipations[0].id) {
            this.selectedRepository = REPOSITORY.ASSIGNMENT;
            this.selectedParticipation = this.exercise.studentParticipations[0] as ProgrammingExerciseStudentParticipation;
            this.selectedParticipation.exercise = exercise;
        } else {
            this.onError('participationNotFound');
        }
    }

    repositoryUrl(participation: Participation) {
        return (participation as ProgrammingExerciseStudentParticipation).repositoryUrl;
    }

    /**
     * Doesn't reload the exercise from server if it was already loaded.
     * This check is done to avoid load on the server when the user is switching participations.
     * Has the side effect, that if the exercise changes unrelated to the participations, the user has to reload the page to see the updates.
     */
    loadExercise(exerciseId: number): Observable<ProgrammingExercise> {
        return this.exercise && this.exercise.id === exerciseId
            ? Observable.of(this.exercise)
            : this.exerciseService.findWithTemplateAndSolutionParticipation(exerciseId).pipe(map(({ body }) => body!));
    }

    /**
     * Set the selected participation domain based on a its id.
     * Shows an error if the participationId does not match the template, solution or assignment participation.
     **/
    selectParticipationDomainById(participationId: number) {
        if (participationId === this.exercise.templateParticipation.id) {
            this.exercise.templateParticipation.programmingExercise = this.exercise;
            this.domainService.setDomain([DomainType.PARTICIPATION, this.exercise.templateParticipation]);
        } else if (participationId === this.exercise.solutionParticipation.id) {
            this.exercise.solutionParticipation.programmingExercise = this.exercise;
            this.domainService.setDomain([DomainType.PARTICIPATION, this.exercise.solutionParticipation]);
        } else if (this.exercise.studentParticipations.length && participationId === this.exercise.studentParticipations[0].id) {
            this.exercise.studentParticipations[0].exercise = this.exercise;
            this.domainService.setDomain([DomainType.PARTICIPATION, this.exercise.studentParticipations[0]]);
        } else {
            this.onError('participationNotFound');
        }
    }

    /**
     * Select the solution participation repository and navigate to it
     */
    abstract selectSolutionParticipation(): void;

    /**
     * Select the template participation repository and navigate to it
     */
    abstract selectTemplateParticipation(): void;

    /**
     * Select the assignment participation repository and navigate to it
     */
    abstract selectAssignmentParticipation(): void;

    /**
     * Select the test repository and navigate to it
     */
    abstract selectTestRepository(): void;

    /**
     * Creates an assignment participation for this user for this exercise.
     */
    createAssignmentParticipation() {
        this.loadingState = LOADING_STATE.CREATING_ASSIGNMENT_REPO;
        return this.courseExerciseService
            .startExercise(this.exercise.course!.id, this.exercise.id)
            .pipe(
                catchError(() => throwError('participationCouldNotBeCreated')),
                tap((participation) => {
                    this.exercise.studentParticipations = [participation];
                    this.loadingState = LOADING_STATE.CLEAR;
                }),
            )
            .subscribe(
                () => {},
                (err) => this.onError(err),
            );
    }

    /**
     * Resets the assignment participation for this user for this exercise.
     * This deletes all build plans, database information, etc. and copies the current version of the template repository.
     */
    resetAssignmentParticipation() {
        this.loadingState = LOADING_STATE.DELETING_ASSIGNMENT_REPO;
        if (this.selectedRepository === REPOSITORY.ASSIGNMENT) {
            this.selectTemplateParticipation();
        }
        const assignmentParticipationId = this.exercise.studentParticipations[0].id;
        this.exercise.studentParticipations = [];
        this.participationService
            .delete(assignmentParticipationId, { deleteBuildPlan: true, deleteRepository: true })
            .pipe(
                catchError(() => throwError('participationCouldNotBeDeleted')),
                tap(() => this.createAssignmentParticipation()),
            )
            .subscribe(
                () => {},
                (err) => this.onError(err),
            );
    }
}
