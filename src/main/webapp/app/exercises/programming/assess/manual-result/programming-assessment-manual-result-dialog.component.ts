import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Result } from 'app/entities/result.model';
import { ResultService } from 'app/exercises/shared/result/result.service';
import { Feedback, FeedbackType } from 'app/entities/feedback.model';
import { JhiEventManager } from 'ng-jhipster';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import * as moment from 'moment';
import { Observable, of } from 'rxjs';
import { ParticipationService } from 'app/exercises/shared/participation/participation.service';
import { catchError, filter, tap } from 'rxjs/operators';
import { ProgrammingAssessmentManualResultService } from 'app/exercises/programming/assess/manual-result/programming-assessment-manual-result.service';
import { SCORE_PATTERN } from 'app/app.constants';
import { Complaint, ComplaintType } from 'app/entities/complaint.model';
import { ComplaintService } from 'app/complaints/complaint.service';
import { AccountService } from 'app/core/auth/account.service';
import { ComplaintResponse } from 'app/entities/complaint-response.model';
import { User } from 'app/core/user/user.model';
import { ProgrammingExercise } from 'app/entities/programming-exercise.model';
import { ProgrammingExerciseStudentParticipation } from 'app/entities/participation/programming-exercise-student-participation.model';
import { AlertService } from 'app/core/alert/alert.service';

@Component({
    selector: 'jhi-exercise-scores-result-dialog',
    templateUrl: './programming-assessment-manual-result-dialog.component.html',
})
export class ProgrammingAssessmentManualResultDialogComponent implements OnInit {
    readonly SCORE_PATTERN = SCORE_PATTERN;
    readonly ComplaintType = ComplaintType;

    @Input() participationId: number;
    @Input() result: Result;
    @Input() exercise: ProgrammingExercise;
    @Output() onResultModified = new EventEmitter<Result>();

    participation: ProgrammingExerciseStudentParticipation;
    feedbacks: Feedback[] = [];
    isLoading = false;
    isSaving = false;
    isOpenForSubmission = false;
    user: User;
    isAssessor: boolean;
    canOverride = false;
    isAtLeastInstructor = false;

    complaint: Complaint;
    resultModified: boolean;

    constructor(
        private participationService: ParticipationService,
        private manualResultService: ProgrammingAssessmentManualResultService,
        private activeModal: NgbActiveModal,
        private eventManager: JhiEventManager,
        private alertService: AlertService,
        private resultService: ResultService,
        private complaintService: ComplaintService,
        private accountService: AccountService,
        private jhiAlertService: AlertService,
    ) {}

    /**
     * Creates or updates a manual result and checks permissions on component initialization
     */
    ngOnInit() {
        // If there already is a manual result, update it instead of creating a new one.
        this.accountService.identity().then((user) => {
            // Used to check if the assessor is the current user
            this.user = user!;
            if (this.result) {
                this.initializeForResultUpdate();
            } else {
                this.initializeForResultCreation();
            }
            this.checkPermissions();
        });
    }

    /**
     * If the result has feedbacks, override the feedbacks of this instance with them.
     *  Else get them from the result service and override the feedbacks of the instance.
     * Get the complaint of this result, if there is one.
     * Override the participation of this instance with its result's.
     */
    initializeForResultUpdate() {
        if (this.result.feedbacks) {
            this.feedbacks = this.result.feedbacks;
        } else {
            this.isLoading = true;
            this.resultService
                .getFeedbackDetailsForResult(this.result.id)
                .pipe(
                    tap(({ body: feedbacks }) => {
                        this.feedbacks = feedbacks!;
                    }),
                )
                .subscribe(() => (this.isLoading = false));
        }
        if (this.result.hasComplaint) {
            this.getComplaint(this.result.id);
        }
        this.participation = this.result.participation! as ProgrammingExerciseStudentParticipation;
    }

    /**
     * Generates a manual result and sets the result of this instance to it.
     * Sets its assessor to the current user.
     */
    initializeForResultCreation() {
        this.isLoading = true;
        this.result = this.manualResultService.generateInitialManualResult();
        this.result.assessor = this.user;
        // TODO: is this call really necessary?
        this.getParticipation();
    }

    private checkPermissions(): void {
        this.isAssessor = this.result.assessor && this.result.assessor.id === this.user.id;
        this.isAtLeastInstructor =
            this.exercise && this.exercise.course
                ? this.accountService.isAtLeastInstructorInCourse(this.exercise.course)
                : this.accountService.hasAnyAuthorityDirect(['ROLE_ADMIN', 'ROLE_INSTRUCTOR']);
        // NOTE: the following line deviates intentionally from other exercises because currently we do not use assessmentDueDate
        // and tutors should be able to override the created results when the assessmentDueDate is not set (also see ResultResource.isAllowedToOverrideExistingResult)
        // TODO: make it consistent with other exercises in the future
        const isBeforeAssessmentDueDate = this.exercise && (!this.exercise.assessmentDueDate || moment().isBefore(this.exercise.assessmentDueDate));
        // tutors are allowed to override one of their assessments before the assessment due date, instructors can override any assessment at any time
        this.canOverride = (this.isAssessor && isBeforeAssessmentDueDate) || this.isAtLeastInstructor;
    }

    /**
     * Gets the participation of this instance from the participation service
     */
    getParticipation() {
        this.participationService
            .find(this.participationId)
            .pipe(
                tap(({ body: participation }) => {
                    this.participation = participation! as ProgrammingExerciseStudentParticipation;
                    this.result.participation = this.participation;
                    this.isOpenForSubmission = this.participation.exercise.dueDate === null || this.participation.exercise.dueDate.isAfter(moment());
                }),
                catchError((err: any) => {
                    this.alertService.error(err);
                    this.clear();
                    return of(null);
                }),
            )
            .subscribe(() => {
                this.isLoading = false;
            });
    }

    /**
     * Emits the result if it was modified and dismisses the activeModal
     */
    clear() {
        if (this.resultModified) {
            this.onResultModified.emit(this.result);
        }
        this.activeModal.dismiss('cancel');
    }

    /**
     * Overrides the feedbacks of the result with the current feedbacks and sets their type to MANUAL
     * Sets isSaving to true
     * Creates or updates this result in the manual result service
     */
    save() {
        this.result.feedbacks = this.feedbacks;
        this.isSaving = true;
        for (let i = 0; i < this.result.feedbacks.length; i++) {
            this.result.feedbacks[i].type = FeedbackType.MANUAL;
        }
        if (this.result.id != null) {
            this.subscribeToSaveResponse(this.manualResultService.update(this.participation.id, this.result));
        } else {
            // in case id is null or undefined
            this.subscribeToSaveResponse(this.manualResultService.create(this.participation.id, this.result));
        }
    }

    private subscribeToSaveResponse(result: Observable<HttpResponse<Result>>) {
        result.subscribe(
            (res) => this.onSaveSuccess(res),
            () => this.onSaveError(),
        );
    }

    /**
     * Closes the active model, sets iSaving to false and broadcasts the corresponding message on a successful save
     * @param {HttpResponse<Result>} result - The HTTP Response with the result
     */
    onSaveSuccess(result: HttpResponse<Result>) {
        this.activeModal.close(result.body);
        this.isSaving = false;
        this.eventManager.broadcast({ name: 'resultListModification', content: 'Added a manual result' });
    }

    /**
     * Only sets isSaving to false
     */
    onSaveError() {
        this.isSaving = false;
    }

    /**
     * Pushes a new feedback to the feedbacks
     */
    pushFeedback() {
        this.feedbacks.push(new Feedback());
    }

    /**
     * Pops a feedback out of feedbacks, if it is not empty
     */
    popFeedback() {
        if (this.feedbacks.length > 0) {
            this.feedbacks.pop();
        }
    }

    private getComplaint(id: number): void {
        this.complaintService
            .findByResultId(id)
            .pipe(filter((res) => !!res.body))
            .subscribe(
                (res) => {
                    this.complaint = res.body!;
                },
                (err: HttpErrorResponse) => {
                    this.alertService.error(err.message);
                },
            );
    }

    /**
     * Sends the current (updated) assessment to the server to update the original assessment after a complaint was accepted.
     * The corresponding complaint response is sent along with the updated assessment to prevent additional requests.
     *
     * @param complaintResponse the response to the complaint that is sent to the server along with the assessment update
     */
    onUpdateAssessmentAfterComplaint(complaintResponse: ComplaintResponse): void {
        this.manualResultService.updateAfterComplaint(this.feedbacks, complaintResponse, this.result, this.result!.submission!.id).subscribe(
            (result: Result) => {
                this.result = result;
                this.resultModified = true;
                this.jhiAlertService.clear();
                this.jhiAlertService.success('artemisApp.assessment.messages.updateAfterComplaintSuccessful');
            },
            () => {
                this.jhiAlertService.clear();
                this.jhiAlertService.error('artemisApp.assessment.messages.updateAfterComplaintFailed');
            },
        );
    }

    /**
     * the dialog is readonly, if it is not writable
     */
    readOnly() {
        return !this.writable();
    }

    /**
     * the dialog is writable if the user can override the result
     * or if there is a complaint that was not yet accepted or rejected
     */
    writable() {
        // TODO: this is still not ideal and we should either distinguish between tutors and instructors here or allow to override accepted / rejected complaints
        // at the moment instructors can still edit already accepted / rejected complaints because the first condition is true, however we do not yet allow to override complaints
        return this.canOverride || (this.complaint !== undefined && this.complaint.accepted === undefined);
    }
}
