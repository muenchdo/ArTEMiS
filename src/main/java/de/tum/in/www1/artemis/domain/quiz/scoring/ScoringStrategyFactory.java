package de.tum.in.www1.artemis.domain.quiz.scoring;

import de.tum.in.www1.artemis.domain.enumeration.ScoringType;
import de.tum.in.www1.artemis.domain.quiz.DragAndDropQuestion;
import de.tum.in.www1.artemis.domain.quiz.MultipleChoiceQuestion;
import de.tum.in.www1.artemis.domain.quiz.QuizQuestion;
import de.tum.in.www1.artemis.domain.quiz.ShortAnswerQuestion;

public class ScoringStrategyFactory {

    /**
     * creates an instance of ScoringStrategy with the appropriate type for the given quizQuestion
     *
     * @param quizQuestion the quizQuestion that needs the ScoringStrategy
     * @return an instance of the appropriate implementation of ScoringStrategy
     */
    public static ScoringStrategy makeScoringStrategy(QuizQuestion quizQuestion) {
        if (quizQuestion instanceof MultipleChoiceQuestion) {
            if (quizQuestion.getScoringType() == ScoringType.ALL_OR_NOTHING) {
                return new ScoringStrategyMultipleChoiceAllOrNothing();
            }
            else if (quizQuestion.getScoringType() == ScoringType.PROPORTIONAL_WITH_PENALTY) {
                return new ScoringStrategyMultipleChoiceProportionalWithPenalty();
            }
        }
        else if (quizQuestion instanceof DragAndDropQuestion) {
            if (quizQuestion.getScoringType() == ScoringType.ALL_OR_NOTHING) {
                return new ScoringStrategyDragAndDropAllOrNothing();
            }
            else if (quizQuestion.getScoringType() == ScoringType.PROPORTIONAL_WITH_PENALTY) {
                return new ScoringStrategyDragAndDropProportionalWithPenalty();
            }
        }
        else if (quizQuestion instanceof ShortAnswerQuestion) {
            if (quizQuestion.getScoringType() == ScoringType.ALL_OR_NOTHING) {
                return new ScoringStrategyShortAnswerAllOrNothing();
            }
            else if (quizQuestion.getScoringType() == ScoringType.PROPORTIONAL_WITH_PENALTY) {
                return new ScoringStrategyShortAnswerProportionalWithPenalty();
            }
        }
        throw new UnsupportedOperationException("Unknown ScoringType!");
    }
}
