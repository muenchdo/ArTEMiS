import { Component, Input } from '@angular/core';
import { HttpErrorResponse, HttpResponse } from '@angular/common/http';
import { JhiEventManager } from 'ng-jhipster';
import { ModelingExercise } from 'app/entities/modeling-exercise.model';
import { ModelingExerciseService } from './modeling-exercise.service';
import { AccountService } from 'app/core/auth/account.service';
import { ActivatedRoute } from '@angular/router';
import { CourseExerciseService, CourseManagementService } from '../../../course/manage/course-management.service';
import { ExerciseComponent } from 'app/exercises/shared/exercise/exercise.component';
import { TranslateService } from '@ngx-translate/core';
import { onError } from 'app/shared/util/global.utils';
import { AlertService } from 'app/core/alert/alert.service';
import { SortService } from 'app/shared/service/sort.service';

@Component({
    selector: 'jhi-modeling-exercise',
    templateUrl: './modeling-exercise.component.html',
})
export class ModelingExerciseComponent extends ExerciseComponent {
    @Input() modelingExercises: ModelingExercise[];

    constructor(
        private modelingExerciseService: ModelingExerciseService,
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
        this.modelingExercises = [];
    }

    protected loadExercises(): void {
        this.courseExerciseService.findAllModelingExercisesForCourse(this.courseId).subscribe(
            (res: HttpResponse<ModelingExercise[]>) => {
                this.modelingExercises = res.body!;
                // reconnect exercise with course
                this.modelingExercises.forEach((exercise) => {
                    exercise.course = this.course;
                    exercise.isAtLeastTutor = this.accountService.isAtLeastTutorInCourse(exercise.course);
                    exercise.isAtLeastInstructor = this.accountService.isAtLeastInstructorInCourse(exercise.course);
                });
                this.emitExerciseCount(this.modelingExercises.length);
            },
            (res: HttpErrorResponse) => onError(this.jhiAlertService, res),
        );
    }

    /**
     * Returns the unique identifier for items in the collection
     * @param index of a modeling exercise in the collection
     * @param item current modeling exercise
     */
    trackId(index: number, item: ModelingExercise) {
        return item.id;
    }

    /**
     * Deletes modeling exercise
     * @param modelingExerciseId id of the exercise that will be deleted
     */
    deleteModelingExercise(modelingExerciseId: number) {
        this.modelingExerciseService.delete(modelingExerciseId).subscribe(
            () => {
                this.eventManager.broadcast({
                    name: 'modelingExerciseListModification',
                    content: 'Deleted an modelingExercise',
                });
                this.dialogErrorSource.next('');
            },
            (error: HttpErrorResponse) => this.dialogErrorSource.next(error.message),
        );
    }

    protected getChangeEventName(): string {
        return 'modelingExerciseListModification';
    }

    sortRows() {
        this.sortService.sortByProperty(this.modelingExercises, this.predicate, this.reverse);
    }
}
