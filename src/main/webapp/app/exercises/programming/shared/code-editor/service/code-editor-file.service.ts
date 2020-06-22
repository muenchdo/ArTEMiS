import { DeleteFileChange, FileChange, RenameFileChange } from 'app/exercises/programming/shared/code-editor/model/code-editor.model';
import { compose, filter, fromPairs, map, toPairs } from 'lodash/fp';
import { isEmpty as _isEmpty } from 'lodash';
import { Injectable } from '@angular/core';

/**
 * Updates references to files based on FileChanges.
 * This includes renaming and deleting files.
 * E.g.:
 * - file refs: {file: any, file2: any}
 * - fileChange: RenameFileChange(file, file3)
 * => file refs: {file3: any, file2: any}
 */
@Injectable({ providedIn: 'root' })
export class CodeEditorFileService {
    /**
     * Update multiple references at once.
     * @param refs
     * @param fileChange
     */
    updateFileReferences = (refs: { [fileName: string]: any }, fileChange: FileChange) => {
        if (fileChange instanceof RenameFileChange) {
            const testRegex = new RegExp(`^${fileChange.oldFileName}($|/.*)`);
            const replaceRegex = new RegExp(`^${fileChange.oldFileName}`);
            return compose(
                fromPairs,
                map(([fileName, refContent]) => [testRegex.test(fileName) ? fileName.replace(replaceRegex, fileChange.newFileName) : fileName, refContent]),
                filter((entry) => !_isEmpty(entry)),
                toPairs,
            )(refs);
        } else if (fileChange instanceof DeleteFileChange) {
            const testRegex = new RegExp(`^${fileChange.fileName}($|/.*)`);
            return compose(
                fromPairs,
                filter(([fileName]) => !testRegex.test(fileName)),
                filter((entry) => !_isEmpty(entry)),
                toPairs,
            )(refs);
        } else {
            return refs;
        }
    };

    /**
     * Update a single reference.
     * @param fileName
     * @param fileChange
     */
    updateFileReference = (fileName: string, fileChange: FileChange) => {
        if (fileChange instanceof RenameFileChange) {
            const testRegex = new RegExp(`^${fileChange.oldFileName}($|/.*)`);
            const replaceRegex = new RegExp(`^${fileChange.oldFileName}`);
            return testRegex.test(fileName) ? fileName.replace(replaceRegex, fileChange.newFileName) : fileName;
        } else if (fileChange instanceof DeleteFileChange) {
            const testRegex = new RegExp(`^${fileChange.fileName}($|/.*)`);
            return testRegex.test(fileName) ? undefined : fileName;
        } else {
            return fileName;
        }
    };
}
