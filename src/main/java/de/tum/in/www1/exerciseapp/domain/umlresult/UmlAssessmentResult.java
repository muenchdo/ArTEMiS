package de.tum.in.www1.exerciseapp.domain.umlresult;


import java.util.List;

/**
 * This class represents the assessment result of a {@link de.tum.in.www1.exerciseapp.domain.enumeration.ExerciseType#UML_CLASS_DIAGRAM}
 * This class is basically used to deserialize from json
 */
public class UmlAssessmentResult {
    private double score;
    private double maxScore;
    private double penaltyScore;
    private List<ErrorMessage> errors;
    private List<ErrorMessage> warnings;
    private List<ErrorMessage> penalties;
    private List<String> correct;
    private List<String> partlyCorrect;

    public double getScore() {
        return score;
    }

    public double getMaxScore() {
        return maxScore;
    }

    public List<ErrorMessage> getErrors() {
        return errors;
    }

    public double getPenaltyScore() {
        return penaltyScore;
    }

    public List<ErrorMessage> getWarnings() {
        return warnings;
    }

    public List<ErrorMessage> getPenalties() {
        return penalties;
    }

    public List<String> getCorrect() {
        return correct;
    }

    public List<String> getPartlyCorrect() {
        return partlyCorrect;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UmlAssessmentResult that = (UmlAssessmentResult) o;

        if (Double.compare(that.score, score) != 0) return false;
        if (Double.compare(that.maxScore, maxScore) != 0) return false;
        if (Double.compare(that.penaltyScore, penaltyScore) != 0) return false;
        if (errors != null ? !errors.equals(that.errors) : that.errors != null) return false;
        if (warnings != null ? !warnings.equals(that.warnings) : that.warnings != null) return false;
        if (penalties != null ? !penalties.equals(that.penalties) : that.penalties != null) return false;
        if (correct != null ? !correct.equals(that.correct) : that.correct != null) return false;
        return partlyCorrect != null ? partlyCorrect.equals(that.partlyCorrect) : that.partlyCorrect == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(score);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxScore);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(penaltyScore);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (errors != null ? errors.hashCode() : 0);
        result = 31 * result + (warnings != null ? warnings.hashCode() : 0);
        result = 31 * result + (penalties != null ? penalties.hashCode() : 0);
        result = 31 * result + (correct != null ? correct.hashCode() : 0);
        result = 31 * result + (partlyCorrect != null ? partlyCorrect.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "UmlAssessmentResult{" +
            "score=" + score +
            ", maxScore=" + maxScore +
            ", penaltyScore=" + penaltyScore +
            ", errors=" + errors +
            ", warnings=" + warnings +
            ", penalties=" + penalties +
            ", correct=" + correct +
            ", partlyCorrect=" + partlyCorrect +
            '}';
    }

    /**
     * Represents an error message produced by uml assessment algorithm
     */
    public static class ErrorMessage {
        private String id;
        private String errorMessage;

        /**
         * Get the id of the element in the UML Diagram that has caused this error
         *
         * @return The id of the UML Diagram element
         */
        public String getId() {
            return id;
        }

        /**
         * The error message
         *
         * @return error message
         */
        public String getErrorMessage() {
            return errorMessage;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ErrorMessage that = (ErrorMessage) o;

            if (id != null ? !id.equals(that.id) : that.id != null) return false;
            return errorMessage != null ? errorMessage.equals(that.errorMessage) : that.errorMessage == null;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (errorMessage != null ? errorMessage.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "ErrorMessage{" +
                "id='" + id + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
        }
    }
}
