import { NgModule } from '@angular/core';

import { ArtemisSharedModule } from 'app/shared/shared.module';
import { MomentModule } from 'ngx-moment';
import { ExerciseScoresComponent } from './exercise-scores.component';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { ArtemisProgrammingAssessmentModule } from 'app/exercises/programming/assess/programming-assessment.module';
import { NgxDatatableModule } from '@swimlane/ngx-datatable';
import { ArtemisDataTableModule } from 'app/shared/data-table/data-table.module';
import { FeatureToggleModule } from 'app/shared/feature-toggle/feature-toggle.module';
import { ProgrammingExerciseUtilsModule } from 'app/exercises/programming/shared/utils/programming-exercise-utils.module';
import { ArtemisResultModule } from 'app/exercises/shared/result/result.module';
import { FormDateTimePickerModule } from 'app/shared/date-time-picker/date-time-picker.module';
import { ArtemisExerciseScoresRoutingModule } from 'app/exercises/shared/exercise-scores/exercise-scores-routing.module';

@NgModule({
    imports: [
        ArtemisSharedModule,
        MomentModule,
        ArtemisExerciseScoresRoutingModule,
        NgbModule,
        ArtemisResultModule,
        FormDateTimePickerModule,
        NgxDatatableModule,
        ArtemisDataTableModule,
        ArtemisProgrammingAssessmentModule,
        FeatureToggleModule,
        ProgrammingExerciseUtilsModule,
    ],
    declarations: [ExerciseScoresComponent],
})
export class ArtemisExerciseScoresModule {}
