// See: https://github.com/ls1intum/Artemis/commit/c842a8995f9f837b010d1ddfa3ebe00df7652011
// We changed the notification plugin to also send information about successful tests (previously only failed tests).
// In some cases it needs to be checked explicitly wether a result is legacy or not.
// The date used is the date of the merge: 2019-05-10T22:12:28Z.
import { Result } from 'app/entities/result.model';
import * as moment from 'moment';
import { isMoment } from 'moment';
import { Participation, ParticipationType } from 'app/entities/participation/participation.model';
import { ProgrammingExercise } from 'app/entities/programming-exercise.model';

const BAMBOO_RESULT_LEGACY_TIMESTAMP = 1557526348000;

export const isLegacyResult = (result: Result) => {
    return result.completionDate!.valueOf() < BAMBOO_RESULT_LEGACY_TIMESTAMP;
};

/**
 * A result is preliminary if:
 * - The programming exercise buildAndTestAfterDueDate is set
 * - The submission date of the result / result completionDate is before the buildAndTestAfterDueDate
 *
 * Note: We check some error cases in this method as a null value for the given parameters, because the clients using this method might unwillingly provide them (result component).
 * TODO: Remove the null checks when the result component is refactored.
 *
 * @param result Result with attached Submission - if submission is null, method will use the result completionDate as a reference.
 * @param programmingExercise ProgrammingExercise
 */
export const isResultPreliminary = (result: Result, programmingExercise: ProgrammingExercise | null) => {
    if (!programmingExercise) {
        return false;
    }
    const { submission } = result;
    // We use the result completionDate as a fallback when the submissionDate is not available (edge case, every result should have a submission).
    let referenceDate = submission && submission.submissionDate ? submission.submissionDate : result.completionDate;
    // If not a moment date already, try to convert it (e.g. when it is a string).
    if (referenceDate && !isMoment(referenceDate)) {
        referenceDate = moment(referenceDate);
    }
    // When the result completionDate would be null, we have to return here (edge case, every result should have a completionDate).
    if (!referenceDate || !referenceDate.isValid()) {
        return false;
    }
    return !!programmingExercise.buildAndTestStudentSubmissionsAfterDueDate && referenceDate.isBefore(moment(programmingExercise.buildAndTestStudentSubmissionsAfterDueDate));
};

export const isProgrammingExerciseStudentParticipation = (participation: Participation) => {
    return participation && participation.type === ParticipationType.PROGRAMMING;
};

/**
 * The deadline has passed if:
 * - The dueDate is set and the buildAndTestAfterDueDate is not set and the dueDate has passed.
 * - The dueDate is set and the buildAndTestAfterDueDate is set and the buildAndTestAfterDueDate has passed.
 *
 * @param exercise
 */
export const hasDeadlinePassed = (exercise: ProgrammingExercise) => {
    // If there is no due date, the due date can't pass.
    if (!exercise.dueDate && !exercise.buildAndTestStudentSubmissionsAfterDueDate) {
        return false;
    }
    // The first priority is the buildAndTestAfterDueDate if it is set.
    let referenceDate = exercise.buildAndTestStudentSubmissionsAfterDueDate || exercise.dueDate!;
    if (!isMoment(referenceDate)) {
        referenceDate = moment(referenceDate);
    }
    return referenceDate.isBefore(moment());
};
