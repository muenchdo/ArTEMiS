import { Command } from './command';

export class CodeCommand extends Command {
    buttonIcon = 'code';
    buttonTranslationString = 'artemisApp.multipleChoiceQuestion.editor.code';

    /**
     * @function execute
     * @desc 1. check if the selected text includes (```language) and/or ('Source Code')
     *       2. if it does include those elements reduce the selected text by this elements and add replace the selected text by the reduced text
     *       3. if it does not include those add (```) before and after the selected text and add them to the text editor
     *       4. code markdown appears
     */
    execute(): void {
        let selectedText = this.getSelectedText();

        if (selectedText.includes('```language ') && !selectedText.includes('Source Code')) {
            const textToAdd = selectedText.slice(12, -3);
            this.insertText(textToAdd);
        } else if (selectedText.includes('```language ') && selectedText.includes('Source Code')) {
            const textToAdd = selectedText.slice(23, -3);
            this.insertText(textToAdd);
        } else {
            const range = this.getRange();
            const initText = 'Source Code';
            selectedText = '```language ' + (selectedText || initText) + '```';
            this.replace(range, selectedText);
            this.focus();
        }
    }
}
