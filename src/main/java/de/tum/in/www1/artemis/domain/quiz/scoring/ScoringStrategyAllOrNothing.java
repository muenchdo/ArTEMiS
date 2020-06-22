package de.tum.in.www1.artemis.domain.quiz.scoring;

import de.tum.in.www1.artemis.domain.SubmittedAnswer;
import de.tum.in.www1.artemis.domain.quiz.QuizQuestion;

public class ScoringStrategyAllOrNothing implements ScoringStrategy {

    // All or nothing means we get the full score if the answer is 100% correct, and 0 points otherwise
    @Override
    public double calculateScore(QuizQuestion quizQuestion, SubmittedAnswer submittedAnswer) {
        if (quizQuestion.isAnswerCorrect(submittedAnswer)) {
            // the answer is 100% correct, so we can return the full score for this quizQuestion
            return quizQuestion.getScore();
        }
        else {
            // the answer is not 100% correct, so we return 0 points
            return 0.0;
        }
    }
}
