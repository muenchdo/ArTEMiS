import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { ArtemisSharedModule } from 'app/shared/shared.module';
import { ArtemisModelingEditorModule } from 'app/exercises/modeling/shared/modeling-editor.module';
import { ArtemisProgrammingExerciseInstructionsRenderModule } from 'app/exercises/programming/shared/instructions-render/programming-exercise-instructions-render.module';
import { ExpandableSectionComponent } from './expandable-section/expandable-section.component';
import { CollapsableAssessmentInstructionsComponent } from './collapsable-assessment-instructions/collapsable-assessment-instructions.component';
import { AssessmentInstructionsComponent } from './assessment-instructions/assessment-instructions.component';
import { ArtemisAssessmentSharedModule } from 'app/assessment/assessment-shared.module';
import { StructuredGradingInstructionsAssessmentLayoutComponent } from 'app/assessment/structured-grading-instructions-assessment-layout/structured-grading-instructions-assessment-layout.component';

@NgModule({
    imports: [CommonModule, NgbModule, ArtemisSharedModule, ArtemisModelingEditorModule, ArtemisProgrammingExerciseInstructionsRenderModule, ArtemisAssessmentSharedModule],
    declarations: [ExpandableSectionComponent, AssessmentInstructionsComponent, CollapsableAssessmentInstructionsComponent, StructuredGradingInstructionsAssessmentLayoutComponent],
    exports: [CollapsableAssessmentInstructionsComponent, ExpandableSectionComponent, StructuredGradingInstructionsAssessmentLayoutComponent, AssessmentInstructionsComponent],
})
export class AssessmentInstructionsModule {}
