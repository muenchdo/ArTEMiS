package de.tum.in.www1.exerciseapp.domain.umlresult;

import de.tum.in.www1.exerciseapp.domain.Participation;

import java.time.ZonedDateTime;

/**
 * Represents the UML Exercise Build result
 * <p>
 * Error State represented by this class:
 * First you have to check if {@link #getInternalErrorMessage()} is not null. If not null, an internal
 * Artemis error has occured and you can display this error message directly.
 * <p>
 * If {@link #getInternalErrorMessage()} is null, then go ahead an check if {@link #isBuildSuccessful()}
 * is false. If false, then Continious Integration has failed. Then the UI should load the continious integration logs.
 *
 * Otherwise, the results can be displayed.
 */
public class UmlBuildResult {
    private boolean buildSuccessful;
    private String parityWithSampleSolution;
    private ZonedDateTime buildCompletionDate;
    private UmlAssessmentResult result;
    private String internalErrorMessage;


    public UmlBuildResult(boolean buildSuccessful,
                          String parityWithSampleSolution,
                          ZonedDateTime buildCompletionDate,
                          UmlAssessmentResult result,
                          String internalErrorMessage) {
        this.buildSuccessful = buildSuccessful;
        this.parityWithSampleSolution = parityWithSampleSolution;
        this.buildCompletionDate = buildCompletionDate;
        this.result = result;
        this.internalErrorMessage = internalErrorMessage;
    }

    /**
     * Is true if Continious Integration build was successful
     * Otherwise: false -> i.e. if Artemis couldn't load Assessment Report from Continious Integration.
     *
     * @return
     * @see #getInternalErrorMessage()
     */
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


    /**
     * If a build has been marked as "not successful", it might have failed because of an internal error of Artemis like
     * Artemis couldn't get the Assessment Report from Continious Integration because Continious Integration is down / offline.
     * <p>
     * Then this method returns an optional error message explaining the reason why Artemis has marked this
     * Build as "failed". Might contain stracktrace. Might be null if the build has failed on Continious Integration.
     *
     * @return
     */
    public String getInternalErrorMessage() {
        return internalErrorMessage;
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
    public String toString() {
        return "UmlBuildResult{" +
            "buildSuccessful=" + buildSuccessful +
            ", parityWithSampleSolution='" + parityWithSampleSolution + '\'' +
            ", buildCompletionDate=" + buildCompletionDate +
            ", result=" + result +
            ", buildNotSuccessfulInternalErrorMessage='" + internalErrorMessage + '\'' +
            '}';
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
        if (result != null ? !result.equals(that.result) : that.result != null) return false;
        return internalErrorMessage != null ? internalErrorMessage.equals(that.internalErrorMessage) : that.internalErrorMessage == null;
    }

    @Override
    public int hashCode() {
        int result1 = (buildSuccessful ? 1 : 0);
        result1 = 31 * result1 + (parityWithSampleSolution != null ? parityWithSampleSolution.hashCode() : 0);
        result1 = 31 * result1 + (buildCompletionDate != null ? buildCompletionDate.hashCode() : 0);
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        result1 = 31 * result1 + (internalErrorMessage != null ? internalErrorMessage.hashCode() : 0);
        return result1;
    }
}


