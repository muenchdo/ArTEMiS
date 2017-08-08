package de.tum.in.www1.exerciseapp.domain.umlresult;

import de.tum.in.www1.exerciseapp.domain.Participation;

import java.time.ZonedDateTime;

/**
 * Represents the UML Exercise Build result
 */
public class UmlBuildResult {
    private boolean buildSuccessful;
    private String parityWithSampleSolution;
    private ZonedDateTime buildCompletionDate;
    private UmlAssessmentResult result;


    public UmlBuildResult(boolean buildSuccessful, String parityWithSampleSolution, ZonedDateTime buildCompletionDate, UmlAssessmentResult result) {
        this.buildSuccessful = buildSuccessful;
        this.parityWithSampleSolution = parityWithSampleSolution;
        this.buildCompletionDate = buildCompletionDate;
        this.result = result;
    }

    public boolean isBuildSuccessful() {
        return buildSuccessful;
    }

    /**
     * The parity with the uml sample solution as string like "84 %"
     *
     * @return
     */
    public String getParityWithSampleSolution() {
        return parityWithSampleSolution;
    }

    public ZonedDateTime getBuildCompletionDate() {
        return buildCompletionDate;
    }

    /**
     * Can be null for performance reasons and requested with another http request.
     * See {@link de.tum.in.www1.exerciseapp.service.ContinuousIntegrationService#getLastUmlExerciseResultDetails(Participation, boolean)}
     *
     * @return
     */
    public UmlAssessmentResult getResult() {
        return result;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UmlBuildResult that = (UmlBuildResult) o;

        if (buildSuccessful != that.buildSuccessful) return false;
        if (parityWithSampleSolution != null ? !parityWithSampleSolution.equals(that.parityWithSampleSolution) : that.parityWithSampleSolution != null)
            return false;
        if (buildCompletionDate != null ? !buildCompletionDate.equals(that.buildCompletionDate) : that.buildCompletionDate != null)
            return false;
        return result != null ? result.equals(that.result) : that.result == null;
    }

    @Override
    public int hashCode() {
        int result1 = (buildSuccessful ? 1 : 0);
        result1 = 31 * result1 + (parityWithSampleSolution != null ? parityWithSampleSolution.hashCode() : 0);
        result1 = 31 * result1 + (buildCompletionDate != null ? buildCompletionDate.hashCode() : 0);
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        return result1;
    }
}


