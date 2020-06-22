import * as chai from 'chai';
import * as sinonChai from 'sinon-chai';
import { ProgrammingExercisePagingService } from 'app/exercises/programming/manage/services/programming-exercise-paging.service';
import { ComponentFixture, fakeAsync, TestBed, tick } from '@angular/core/testing';
import { DebugElement } from '@angular/core';
import { Course } from 'app/entities/course.model';
import { TranslateModule } from '@ngx-translate/core';
import { ArtemisTestModule } from '../../test.module';
import { SinonStub, stub } from 'sinon';
import { Subject } from 'rxjs';
import { DifferencePipe } from 'ngx-moment';
import { FeatureToggleModule } from 'app/shared/feature-toggle/feature-toggle.module';
import { FeatureToggleService } from 'app/shared/feature-toggle/feature-toggle.service';
import { MockFeatureToggleService } from '../../helpers/mocks/service/mock-feature-toggle.service';
import { ProgrammingExerciseImportComponent } from 'app/exercises/programming/manage/programming-exercise-import.component';
import { ArtemisSharedCommonModule } from 'app/shared/shared-common.module';
import { ProgrammingExercise, ProgrammingLanguage } from 'app/entities/programming-exercise.model';
import { SearchResult } from 'app/shared/table/pageable-table';
import { ButtonComponent } from 'app/shared/components/button.component';
import { MockProgrammingExercisePagingService } from '../../helpers/mocks/service/mock-programming-exercise-paging.service';
import { ArtemisSharedPipesModule } from 'app/shared/pipes/shared-pipes.module';

chai.use(sinonChai);
const expect = chai.expect;

describe('ProgrammingExerciseImportComponent', () => {
    let comp: ProgrammingExerciseImportComponent;
    let fixture: ComponentFixture<ProgrammingExerciseImportComponent>;
    let debugElement: DebugElement;
    let pagingService: ProgrammingExercisePagingService;

    let pagingStub: SinonStub;

    const basicCourse = { id: 12, title: 'Random course title' } as Course;
    const exercise = { id: 42, title: 'Exercise title', programmingLanguage: ProgrammingLanguage.JAVA, course: basicCourse } as ProgrammingExercise;

    beforeEach(async () => {
        return TestBed.configureTestingModule({
            imports: [TranslateModule.forRoot(), ArtemisTestModule, ArtemisSharedCommonModule, FeatureToggleModule, ArtemisSharedPipesModule],
            declarations: [ProgrammingExerciseImportComponent, ButtonComponent],
            providers: [
                DifferencePipe,
                { provide: ProgrammingExercisePagingService, useClass: MockProgrammingExercisePagingService },
                { provide: FeatureToggleService, useClass: MockFeatureToggleService },
            ],
        })
            .overrideModule(ArtemisTestModule, { set: { declarations: [], exports: [] } })
            .compileComponents()
            .then(() => {
                fixture = TestBed.createComponent(ProgrammingExerciseImportComponent);
                comp = fixture.componentInstance;
                debugElement = fixture.debugElement;
                pagingService = debugElement.injector.get(ProgrammingExercisePagingService);
                pagingStub = stub(pagingService, 'searchForExercises');
            });
    });

    afterEach(() => {
        pagingStub.restore();
    });

    it('should parse the pageable search result into the correct state', fakeAsync(() => {
        const searchResult = { resultsOnPage: [exercise], numberOfPages: 3 } as SearchResult<ProgrammingExercise>;
        const searchObservable = new Subject<SearchResult<ProgrammingExercise>>();
        pagingStub.returns(searchObservable);

        fixture.detectChanges();
        tick();

        expect(pagingStub).to.have.been.calledOnce;
        searchObservable.next(searchResult);

        fixture.detectChanges();
        tick();

        expect(comp.content.numberOfPages).to.be.eq(3);
        expect(comp.content.resultsOnPage[0].id).to.be.eq(42);
        expect(comp.total).to.be.eq(30);
        expect(comp.loading).to.be.false;
    }));
});
