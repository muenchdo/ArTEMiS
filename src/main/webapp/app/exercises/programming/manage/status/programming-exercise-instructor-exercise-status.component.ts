import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { Subscription } from 'rxjs';
import { filter, tap } from 'rxjs/operators';
import { ParticipationWebsocketService } from 'app/overview/participation-websocket.service';
import { Participation } from 'app/entities/participation/participation.model';
import { ProgrammingExercise } from 'app/entities/programming-exercise.model';
import { hasSolutionParticipationChanged, hasTemplateParticipationChanged } from 'app/overview/participation-utils';

/**
 * Describes programming exercise issues
 */
enum ProgrammingExerciseIssues {
    TEMPLATE_PASSING = 'TEMPLATE_PASSING',
    SOLUTION_FAILING = 'SOLUTION_FAILING',
}

@Component({
    selector: 'jhi-programming-exercise-instructor-exercise-status',
    templateUrl: './programming-exercise-instructor-exercise-status.component.html',
})
export class ProgrammingExerciseInstructorExerciseStatusComponent implements OnChanges {
    ProgrammingExerciseIssues = ProgrammingExerciseIssues;
    @Input() templateParticipation: Participation;
    @Input() solutionParticipation: Participation;
    @Input() exercise: ProgrammingExercise;

    templateParticipationSubscription: Subscription;
    solutionParticipationSubscription: Subscription;
    issues: (ProgrammingExerciseIssues | null)[] = [];

    constructor(private participationWebsocketService: ParticipationWebsocketService) {}

    /**
     * If there are changes for the template or solution participation, check if there are issues
     * for the results of the template or solution participation
     * @param changes The hashtable of occurred changes represented as SimpleChanges object.
     */
    ngOnChanges(changes: SimpleChanges): void {
        if (hasTemplateParticipationChanged(changes)) {
            if (this.templateParticipationSubscription) {
                this.templateParticipationSubscription.unsubscribe();
            }
            this.templateParticipationSubscription = this.participationWebsocketService
                .subscribeForLatestResultOfParticipation(this.templateParticipation.id, false, this.exercise.id)
                .pipe(
                    filter((result) => !!result),
                    tap((result) => (this.templateParticipation.results = [result!])),
                    tap(() => this.findIssues()),
                )
                .subscribe();
        }

        if (hasSolutionParticipationChanged(changes)) {
            if (this.solutionParticipationSubscription) {
                this.solutionParticipationSubscription.unsubscribe();
            }
            this.solutionParticipationSubscription = this.participationWebsocketService
                .subscribeForLatestResultOfParticipation(this.solutionParticipation.id, false, this.exercise.id)
                .pipe(
                    filter((result) => !!result),
                    tap((result) => (this.solutionParticipation.results = [result!])),
                    tap(() => this.findIssues()),
                )
                .subscribe();
        }

        this.findIssues();
    }

    /**
     * Finds issues of the template and the solution participation result
     */
    findIssues() {
        const newestTemplateParticipationResult = this.templateParticipation.results ? this.templateParticipation.results.reduce((acc, x) => (acc.id > x.id ? acc : x)) : null;
        const newestSolutionParticipationResult = this.solutionParticipation.results ? this.solutionParticipation.results.reduce((acc, x) => (acc.id > x.id ? acc : x)) : null;

        this.issues = [
            newestTemplateParticipationResult && newestTemplateParticipationResult.score > 0 ? ProgrammingExerciseIssues.TEMPLATE_PASSING : null,
            newestSolutionParticipationResult && !newestSolutionParticipationResult.successful ? ProgrammingExerciseIssues.SOLUTION_FAILING : null,
        ].filter(Boolean);
    }
}
