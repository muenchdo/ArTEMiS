package de.tum.in.www1.artemis.domain.quiz.scoring;

import de.tum.in.www1.artemis.domain.*;
import de.tum.in.www1.artemis.domain.quiz.*;

/**
 * All or nothing means the full score is given if the answer is 100% correct, otherwise a score of 0 is given
 */
public class ScoringStrategyDragAndDropAllOrNothing implements ScoringStrategy {

    @Override
    public double calculateScore(QuizQuestion quizQuestion, SubmittedAnswer submittedAnswer) {
        // return maximal Score if the quizQuestion is invalid
        if (quizQuestion.isInvalid()) {
            return quizQuestion.getScore();
        }
        if (submittedAnswer instanceof DragAndDropSubmittedAnswer && quizQuestion instanceof DragAndDropQuestion) {
            DragAndDropSubmittedAnswer dndAnswer = (DragAndDropSubmittedAnswer) submittedAnswer;
            DragAndDropQuestion dndQuestion = (DragAndDropQuestion) quizQuestion;
            // iterate through each drop location and compare its correct mappings with the answer's mapping
            for (DropLocation dropLocation : dndQuestion.getDropLocations()) {
                DragItem selectedDragItem = dndAnswer.getSelectedDragItemForDropLocation(dropLocation);
                // return 0.0 if an dropLocation is solved incorrect and the dropLocation and dragItem is valid
                if (!dropLocation.isInvalid() && !(selectedDragItem != null && selectedDragItem.isInvalid()) && !dropLocation.isDropLocationCorrect(dndAnswer)) {
                    return 0.0;
                }
            }
            // the user wasn't wrong about a single drop location => the answer is 100% correct
            return dndQuestion.getScore();
        }
        // the submitted answer's type doesn't fit the quizQuestion's type => it cannot be correct
        return 0.0;
    }
}
