import { Component } from '@angular/core';

@Component({
    selector: 'jhi-code-editor-repository-is-locked',
    template: `
        <span class="badge badge-warning d-flex align-items-center locked-container">
            <fa-icon [icon]="'info-circle'" class="text-white" size="2x"></fa-icon>
            <span
                class="ml-2 locked-lable"
                jhiTranslate="artemisApp.programmingExercise.repositoryIsLocked.title"
                ngbTooltip="{{ 'artemisApp.programmingExercise.repositoryIsLocked.tooltip' | translate }}"
            >
                The due date has passed, your repository is locked. You can still read the code but not make any changes to it.
            </span>
        </span>
    `,
    styles: ['.locked-lable {font-size: 1.2rem; color: white}'],
})
export class CodeEditorRepositoryIsLockedComponent {}
