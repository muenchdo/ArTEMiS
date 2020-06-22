import { BaseEntity } from 'app/shared/model/base-entity';
import { ShortAnswerQuestion } from 'app/entities/quiz/short-answer-question.model';
import { ShortAnswerSpot } from 'app/entities/quiz/short-answer-spot.model';
import { ShortAnswerSolution } from 'app/entities/quiz/short-answer-solution.model';

export class ShortAnswerMapping implements BaseEntity {
    public id: number;
    public shortAnswerSpotIndex: number;
    public shortAnswerSolutionIndex: number;
    public invalid = false; // default value
    public solution: ShortAnswerSolution;
    public spot: ShortAnswerSpot;
    public question: ShortAnswerQuestion;

    constructor(spot: ShortAnswerSpot, solution: ShortAnswerSolution) {
        this.spot = spot;
        this.solution = solution;
    }
}
