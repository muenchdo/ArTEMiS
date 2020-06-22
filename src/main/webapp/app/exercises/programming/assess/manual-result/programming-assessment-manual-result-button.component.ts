import { Component, EventEmitter, Input, OnChanges, OnDestroy, Output, SimpleChanges } from '@angular/core';
import { NgbModal, NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { ProgrammingAssessmentManualResultDialogComponent } from 'app/exercises/programming/assess/manual-result/programming-assessment-manual-result-dialog.component';
import { Result } from 'app/entities/result.model';
import { Subscription } from 'rxjs';
import { ParticipationWebsocketService } from 'app/overview/participation-websocket.service';
import { filter } from 'rxjs/operators';
import { cloneDeep } from 'lodash';
import { User } from 'app/core/user/user.model';
import { ButtonSize, ButtonType } from 'app/shared/components/button.component';
import { AssessmentType } from 'app/entities/assessment-type.model';
import { ProgrammingExercise } from 'app/entities/programming-exercise.model';

@Component({
    selector: 'jhi-programming-assessment-manual-result',
    template: `
        <jhi-button
            [disabled]="!participationId"
            [btnType]="ButtonType.WARNING"
            [btnSize]="ButtonSize.SMALL"
            [icon]="'asterisk'"
            [title]="latestResult ? (latestResult.hasComplaint ? 'entity.action.viewResult' : 'entity.action.updateResult') : 'entity.action.newResult'"
            (onClick)="openManualResultDialog($event)"
        ></jhi-button>
    `,
})
export class ProgrammingAssessmentManualResultButtonComponent implements OnChanges, OnDestroy {
    ButtonType = ButtonType;
    ButtonSize = ButtonSize;
    @Input() participationId: number;
    @Output() onResultModified = new EventEmitter<Result>();
    @Input() latestResult?: Result | null;
    @Input() exercise: ProgrammingExercise;

    latestResultSubscription: Subscription;

    constructor(private modalService: NgbModal, private participationWebsocketService: ParticipationWebsocketService) {}

    /**
     * - Check that the inserted result is of type MANUAL, otherwise set it to null
     * - If the participationId changes, subscribe to the latest result from the websocket
     *
     * @param changes
     */
    ngOnChanges(changes: SimpleChanges): void {
        if (changes.latestResult && this.latestResult && this.latestResult.assessmentType !== AssessmentType.MANUAL) {
            // The assessor can't update the automatic result of the student.
            this.latestResult = null;
        }
        if (changes.participationId && this.participationId) {
            if (this.latestResultSubscription) {
                this.latestResultSubscription.unsubscribe();
            }
            this.latestResultSubscription = this.participationWebsocketService
                .subscribeForLatestResultOfParticipation(this.participationId, false, this.exercise.id)
                .pipe(filter((result: Result) => result && result.assessmentType === AssessmentType.MANUAL))
                .subscribe((manualResult) => {
                    let assessor: User | null = null;
                    // TODO: workaround to fix an issue when the assessor gets lost due to the websocket update
                    // we should properly fix this in the future and make sure the assessor is not cut off in the first place
                    if (this.latestResult && this.latestResult.assessor && this.latestResult.id === manualResult.id) {
                        assessor = this.latestResult.assessor;
                    }
                    this.latestResult = manualResult;
                    if (assessor && !this.latestResult.assessor) {
                        this.latestResult.assessor = assessor;
                    }
                });
        }
    }

    /**
     * Unsubscribes this instance, if it is the latest result submission
     */
    ngOnDestroy(): void {
        if (this.latestResultSubscription) {
            this.latestResultSubscription.unsubscribe();
        }
    }

    /**
     * Stops the propagation of the mouse event, updates the component instance of the modalRef with
     * this instance's values and emits the result if it is modified
     * @param {MouseEvent} event - Mouse event
     */
    openManualResultDialog(event: MouseEvent) {
        event.stopPropagation();
        const modalRef: NgbModalRef = this.modalService.open(ProgrammingAssessmentManualResultDialogComponent, { keyboard: true, size: 'lg', backdrop: 'static' });
        modalRef.componentInstance.participationId = this.participationId;
        modalRef.componentInstance.result = cloneDeep(this.latestResult);
        modalRef.componentInstance.exercise = this.exercise;
        modalRef.componentInstance.onResultModified.subscribe(($event: Result) => this.onResultModified.emit($event));
        modalRef.result.then(
            (result) => this.onResultModified.emit(result),
            () => {},
        );
    }
}
