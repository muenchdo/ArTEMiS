import { Component, Input } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { JhiEventManager } from 'ng-jhipster';
import { TranslateService } from '@ngx-translate/core';
import { filter } from 'rxjs/operators';
import { FileUploadExercise } from 'app/entities/file-upload-exercise.model';
import { FileUploadExerciseService } from './file-upload-exercise.service';
import { ExerciseComponent } from 'app/exercises/shared/exercise/exercise.component';
import { onError } from 'app/shared/util/global.utils';
import { AccountService } from 'app/core/auth/account.service';
import { AlertService } from 'app/core/alert/alert.service';
import { CourseExerciseService, CourseManagementService } from '../../../course/manage/course-management.service';
import { SortService } from 'app/shared/service/sort.service';

@Component({
    selector: 'jhi-file-upload-exercise',
    templateUrl: './file-upload-exercise.component.html',
})
export class FileUploadExerciseComponent extends ExerciseComponent {
    @Input() fileUploadExercises: FileUploadExercise[] = [];

    constructor(
        private fileUploadExerciseService: FileUploadExerciseService,
        private courseExerciseService: CourseExerciseService,
        private jhiAlertService: AlertService,
        private accountService: AccountService,
        private sortService: SortService,
        courseService: CourseManagementService,
        translateService: TranslateService,
        eventManager: JhiEventManager,
        route: ActivatedRoute,
    ) {
        super(courseService, translateService, route, eventManager);
    }

    protected loadExercises(): void {
        this.courseExerciseService
            .findAllFileUploadExercisesForCourse(this.courseId)
            .pipe(filter((res) => !!res.body))
            .subscribe(
                (res: HttpResponse<FileUploadExercise[]>) => {
                    this.fileUploadExercises = res.body!;
                    // reconnect exercise with course
                    this.fileUploadExercises.forEach((exercise) => {
                        exercise.course = this.course;
                        exercise.isAtLeastTutor = this.accountService.isAtLeastTutorInCourse(exercise.course);
                        exercise.isAtLeastInstructor = this.accountService.isAtLeastInstructorInCourse(exercise.course);
                    });
                    this.emitExerciseCount(this.fileUploadExercises.length);
                },
                (res: HttpErrorResponse) => onError(this.jhiAlertService, res),
            );
    }

    /**
     * Returns the unique identifier for items in the collection
     * @param index of a file upload exercise in the collection
     * @param item current file upload exercise
     */
    trackId(index: number, item: FileUploadExercise) {
        return item.id;
    }

    /**
     * Deletes file upload exercise
     * @param fileUploadExerciseId id of the exercise that will be deleted
     */
    deleteFileUploadExercise(fileUploadExerciseId: number) {
        this.fileUploadExerciseService.delete(fileUploadExerciseId).subscribe(
            () => {
                this.eventManager.broadcast({
                    name: 'fileUploadExerciseListModification',
                    content: 'Deleted an fileUploadExercise',
                });
                this.dialogErrorSource.next('');
            },
            (error: HttpErrorResponse) => this.dialogErrorSource.next(error.message),
        );
    }

    protected getChangeEventName(): string {
        return 'fileUploadExerciseListModification';
    }

    sortRows() {
        this.sortService.sortByProperty(this.fileUploadExercises, this.predicate, this.reverse);
    }
}
