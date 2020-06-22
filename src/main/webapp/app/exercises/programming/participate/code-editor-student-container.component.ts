import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { Observable } from 'rxjs';
import { Subscription } from 'rxjs/Subscription';
import { catchError, flatMap, map, tap } from 'rxjs/operators';
import * as moment from 'moment';
import { ParticipationService } from 'app/exercises/shared/participation/participation.service';
import { TranslateService } from '@ngx-translate/core';
import { ActivatedRoute } from '@angular/router';
import { AlertService } from 'app/core/alert/alert.service';
import { ProgrammingExerciseParticipationService } from 'app/exercises/programming/manage/services/programming-exercise-participation.service';
import { GuidedTourService } from 'app/guided-tour/guided-tour.service';
import { codeEditorTour } from 'app/guided-tour/tours/code-editor-tour';
import { CodeEditorBuildOutputComponent } from 'app/exercises/programming/shared/code-editor/build-output/code-editor-build-output.component';
import { ButtonSize } from 'app/shared/components/button.component';
import { CodeEditorSessionService } from 'app/exercises/programming/shared/code-editor/service/code-editor-session.service';
import { ResultService } from 'app/exercises/shared/result/result.service';
import { DomainService } from 'app/exercises/programming/shared/code-editor/service/code-editor-domain.service';
import { CodeEditorFileService } from 'app/exercises/programming/shared/code-editor/service/code-editor-file.service';
import { CodeEditorActionsComponent } from 'app/exercises/programming/shared/code-editor/actions/code-editor-actions.component';
import { CodeEditorAceComponent } from 'app/exercises/programming/shared/code-editor/ace/code-editor-ace.component';
import { ExerciseType } from 'app/entities/exercise.model';
import { StudentParticipation } from 'app/entities/participation/student-participation.model';
import { Result } from 'app/entities/result.model';
import { CodeEditorContainer } from 'app/exercises/programming/shared/code-editor/code-editor-mode-container.component';
import { Feedback } from 'app/entities/feedback.model';
import { CodeEditorInstructionsComponent } from 'app/exercises/programming/shared/code-editor/instructions/code-editor-instructions.component';
import { CodeEditorFileBrowserComponent } from 'app/exercises/programming/shared/code-editor/file-browser/code-editor-file-browser.component';
import { ProgrammingExercise } from 'app/entities/programming-exercise.model';
import { DomainType } from 'app/exercises/programming/shared/code-editor/model/code-editor.model';

@Component({
    selector: 'jhi-code-editor-student',
    templateUrl: './code-editor-student-container.component.html',
})
export class CodeEditorStudentContainerComponent extends CodeEditorContainer implements OnInit, OnDestroy {
    @ViewChild(CodeEditorFileBrowserComponent, { static: false }) fileBrowser: CodeEditorFileBrowserComponent;
    @ViewChild(CodeEditorActionsComponent, { static: false }) actions: CodeEditorActionsComponent;
    @ViewChild(CodeEditorBuildOutputComponent, { static: false }) buildOutput: CodeEditorBuildOutputComponent;
    @ViewChild(CodeEditorInstructionsComponent, { static: false }) instructions: CodeEditorInstructionsComponent;
    @ViewChild(CodeEditorAceComponent, { static: false }) aceEditor: CodeEditorAceComponent;

    ButtonSize = ButtonSize;
    PROGRAMMING = ExerciseType.PROGRAMMING;

    paramSub: Subscription;
    participation: StudentParticipation;
    exercise: ProgrammingExercise;

    // Fatal error state: when the participation can't be retrieved, the code editor is unusable for the student
    loadingParticipation = false;
    participationCouldNotBeFetched = false;
    repositoryIsLocked = false;

    constructor(
        private resultService: ResultService,
        private domainService: DomainService,
        private programmingExerciseParticipationService: ProgrammingExerciseParticipationService,
        private guidedTourService: GuidedTourService,
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
     * On init set up the route param subscription.
     * Will load the participation according to participation Id with the latest result and result details.
     */
    ngOnInit(): void {
        this.paramSub = this.route.params.subscribe((params) => {
            this.loadingParticipation = true;
            this.participationCouldNotBeFetched = false;
            const participationId = Number(params['participationId']);
            this.loadParticipationWithLatestResult(participationId)
                .pipe(
                    tap((participationWithResults) => {
                        this.domainService.setDomain([DomainType.PARTICIPATION, participationWithResults!]);
                        this.participation = participationWithResults!;
                        this.exercise = this.participation.exercise as ProgrammingExercise;
                        // We lock the repository when the buildAndTestAfterDueDate is set and the due date has passed.
                        const dueDateHasPassed = !this.exercise.dueDate || moment(this.exercise.dueDate).isBefore(moment());
                        this.repositoryIsLocked = !!this.exercise.buildAndTestStudentSubmissionsAfterDueDate && !!this.exercise.dueDate && dueDateHasPassed;
                    }),
                )
                .subscribe(
                    () => {
                        this.loadingParticipation = false;
                        this.guidedTourService.enableTourForExercise(this.exercise, codeEditorTour, true);
                    },
                    () => {
                        this.participationCouldNotBeFetched = true;
                        this.loadingParticipation = false;
                    },
                );
        });
    }

    /**
     * If a subscription exists for paramSub, unsubscribe
     */
    ngOnDestroy() {
        if (this.paramSub) {
            this.paramSub.unsubscribe();
        }
    }

    /**
     * Load the participation from server with the latest result.
     * @param participationId
     */
    loadParticipationWithLatestResult(participationId: number): Observable<StudentParticipation | null> {
        return this.programmingExerciseParticipationService.getStudentParticipationWithLatestResult(participationId).pipe(
            flatMap((participation: StudentParticipation) =>
                participation.results && participation.results.length
                    ? this.loadResultDetails(participation.results[0]).pipe(
                          map((feedback) => {
                              if (feedback) {
                                  participation.results[0].feedbacks = feedback;
                              }
                              return participation;
                          }),
                          catchError(() => Observable.of(participation)),
                      )
                    : Observable.of(participation),
            ),
        );
    }

    /**
     * Fetches details for the result (if we received one) and attach them to the result.
     * Mutates the input parameter result.
     */
    loadResultDetails(result: Result): Observable<Feedback[] | null> {
        return this.resultService.getFeedbackDetailsForResult(result.id).pipe(map((res) => res && res.body));
    }
}
