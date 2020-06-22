import { Component, EventEmitter, Input, OnDestroy, OnInit, Output } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { catchError, switchMap, tap } from 'rxjs/operators';
import { Observable, of, Subscription, throwError } from 'rxjs';
import { isEmpty as _isEmpty } from 'lodash';
import { CodeEditorSubmissionService } from 'app/exercises/programming/shared/code-editor/service/code-editor-submission.service';
import { CodeEditorConflictStateService } from 'app/exercises/programming/shared/code-editor/service/code-editor-conflict-state.service';
import { CodeEditorResolveConflictModalComponent } from 'app/exercises/programming/shared/code-editor/actions/code-editor-resolve-conflict-modal.component';
import { FeatureToggle } from 'app/shared/feature-toggle/feature-toggle.service';
import { CodeEditorRepositoryFileService, CodeEditorRepositoryService } from 'app/exercises/programming/shared/code-editor/service/code-editor-repository.service';
import { CommitState, EditorState, GitConflictState } from 'app/exercises/programming/shared/code-editor/model/code-editor.model';

@Component({
    selector: 'jhi-code-editor-actions',
    templateUrl: './code-editor-actions.component.html',
})
export class CodeEditorActionsComponent implements OnInit, OnDestroy {
    CommitState = CommitState;
    EditorState = EditorState;
    FeatureToggle = FeatureToggle;

    @Input()
    buildable = true;
    @Input()
    unsavedFiles: { [fileName: string]: string };
    @Input() disableActions = false;
    @Input()
    get editorState() {
        return this.editorStateValue;
    }
    @Input()
    get commitState() {
        return this.commitStateValue;
    }

    @Output()
    commitStateChange = new EventEmitter<CommitState>();
    @Output()
    editorStateChange = new EventEmitter<EditorState>();
    @Output()
    isBuildingChange = new EventEmitter<boolean>();
    @Output()
    onSavedFiles = new EventEmitter<{ [fileName: string]: string | null }>();
    @Output()
    onError = new EventEmitter<string>();

    isBuilding: boolean;
    editorStateValue: EditorState;
    commitStateValue: CommitState;
    isResolvingConflict = false;

    conflictStateSubscription: Subscription;
    submissionSubscription: Subscription;

    set commitState(commitState: CommitState) {
        this.commitStateValue = commitState;
        this.commitStateChange.emit(commitState);
    }

    set editorState(editorState: EditorState) {
        this.editorStateValue = editorState;
        this.editorStateChange.emit(editorState);
    }

    constructor(
        private repositoryService: CodeEditorRepositoryService,
        private repositoryFileService: CodeEditorRepositoryFileService,
        private conflictService: CodeEditorConflictStateService,
        private modalService: NgbModal,
        private submissionService: CodeEditorSubmissionService,
    ) {}

    ngOnInit(): void {
        this.conflictStateSubscription = this.conflictService.subscribeConflictState().subscribe((gitConflictState: GitConflictState) => {
            // When the conflict is encountered when opening the code-editor, setting the commitState here could cause an uncheckedException.
            // This is why a timeout of 0 is set to make sure the template is rendered before setting the commitState.
            if (this.commitState === CommitState.CONFLICT && gitConflictState === GitConflictState.OK) {
                // Case a: Conflict was resolved.
                setTimeout(() => (this.commitState = CommitState.UNDEFINED), 0);
            } else if (this.commitState !== CommitState.CONFLICT && gitConflictState === GitConflictState.CHECKOUT_CONFLICT) {
                // Case b: Conflict has occurred.
                setTimeout(() => (this.commitState = CommitState.CONFLICT), 0);
            }
        });
        this.submissionSubscription = this.submissionService
            .getBuildingState()
            .pipe(tap((isBuilding: boolean) => (this.isBuilding = isBuilding)))
            .subscribe();
    }

    ngOnDestroy(): void {
        if (this.conflictStateSubscription) {
            this.conflictStateSubscription.unsubscribe();
        }
    }

    onSave() {
        this.saveChangedFiles()
            .pipe(catchError(() => of()))
            .subscribe();
    }

    /**
     * @function saveFiles
     * @desc Saves all files that have unsaved changes in the editor.
     */
    saveChangedFiles(): Observable<any> {
        if (!_isEmpty(this.unsavedFiles)) {
            this.editorState = EditorState.SAVING;
            const unsavedFiles = Object.entries(this.unsavedFiles).map(([fileName, fileContent]) => ({ fileName, fileContent }));
            return this.repositoryFileService.updateFiles(unsavedFiles).pipe(
                tap((res) => this.onSavedFiles.emit(res)),
                catchError((err) => {
                    this.onError.emit(err.error);
                    this.editorState = EditorState.UNSAVED_CHANGES;
                    return throwError('saving failed');
                }),
            );
        } else {
            return Observable.of(null);
        }
    }

    /**
     * @function commit
     * @desc Commits the current repository files.
     * If there are unsaved changes, save them first before trying to commit again.
     */
    commit() {
        // Avoid multiple commits at the same time.
        if (this.commitState === CommitState.COMMITTING) {
            return;
        }
        // If there are unsaved changes, save them before trying to commit again.
        Observable.of(null)
            .pipe(
                switchMap(() => (this.editorState === EditorState.UNSAVED_CHANGES ? this.saveChangedFiles() : Observable.of(null))),
                tap(() => (this.commitState = CommitState.COMMITTING)),
                switchMap(() => this.repositoryService.commit()),
                tap(() => {
                    this.commitState = CommitState.CLEAN;
                    // Note: this is not 100% clean, but not setting it here would complicate the state model.
                    // We just assume that after the commit a build happens if the repo is buildable.
                    if (this.buildable) {
                        this.isBuilding = true;
                    }
                }),
            )
            .subscribe(
                () => {},
                () => {
                    this.commitState = CommitState.UNCOMMITTED_CHANGES;
                    this.onError.emit('commitFailed');
                },
            );
    }

    resetRepository() {
        this.modalService.open(CodeEditorResolveConflictModalComponent, { keyboard: true, size: 'lg' });
    }
}
