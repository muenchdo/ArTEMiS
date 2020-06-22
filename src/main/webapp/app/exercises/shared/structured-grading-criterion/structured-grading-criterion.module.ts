import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ArtemisSharedModule } from 'app/shared/shared.module';
import { ArtemisMarkdownEditorModule } from 'app/shared/markdown-editor/markdown-editor.module';
import { GradingInstructionsDetailsComponent } from 'app/exercises/shared/structured-grading-criterion/grading-instructions-details/grading-instructions-details.component.ts';

@NgModule({
    declarations: [GradingInstructionsDetailsComponent],
    exports: [GradingInstructionsDetailsComponent],
    imports: [CommonModule, ArtemisSharedModule, ArtemisMarkdownEditorModule],
})
export class StructuredGradingCriterionModule {}
