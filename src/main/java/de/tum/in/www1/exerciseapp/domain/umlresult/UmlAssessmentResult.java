package de.tum.in.www1.exerciseapp.domain.umlresult;


import java.util.List;

/**
 * This class represents the assessment result of a {@link de.tum.in.www1.exerciseapp.domain.enumeration.ExerciseType#UML_CLASS_DIAGRAM}
 * This class is basically used to deserialize from json
 */
public class UmlAssessmentResult {
    private double score;
    private double maxScore;
    private int numberOfErrors;
    private int numberOfTests;
    private List<ErrorMessage> errors;

    public double getScore() {
        return score;
    }

    public double getMaxScore() {
        return maxScore;
    }

    /**
     * The number of failed tests
     * @return
     */
    public int getNumberOfErrors() {
        return numberOfErrors;
    }

    public List<ErrorMessage> getErrors() {
        return errors;
    }


    /**
     * Get the number of how many tests were run int total
     * @return
     */
    public int getNumberOfTests() {
        return numberOfTests;
    }

    @Override
    public String toString() {
        return "UmlAssessmentResult{" +
            "score=" + score +
            ", maxScore=" + maxScore +
            ", numberOfErrors=" + numberOfErrors +
            ", numberOfTests=" + numberOfTests +
            ", errors=" + errors +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UmlAssessmentResult that = (UmlAssessmentResult) o;

        if (Double.compare(that.score, score) != 0) return false;
        if (Double.compare(that.maxScore, maxScore) != 0) return false;
        if (numberOfErrors != that.numberOfErrors) return false;
        if (numberOfTests != that.numberOfTests) return false;
        return errors != null ? errors.equals(that.errors) : that.errors == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(score);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(maxScore);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + numberOfErrors;
        result = 31 * result + numberOfTests;
        result = 31 * result + (errors != null ? errors.hashCode() : 0);
        return result;
    }

    /**
     * Represents an error message produced by uml assessment algorithm
     */
    public static class ErrorMessage {
        private String id;
        private String errorMessage;
        private boolean warning;

        /**
         * Get the id of the element in the UML Diagram that has caused this error
         * @return The id of the UML Diagram element
         */
        public String getId() {
            return id;
        }

        /**
         * The error message
         * @return error message
         */
        public String getErrorMessage() {
            return errorMessage;
        }

        public boolean isWarning() {
            return warning;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ErrorMessage that = (ErrorMessage) o;

            if (warning != that.warning) return false;
            if (id != null ? !id.equals(that.id) : that.id != null) return false;
            return errorMessage != null ? errorMessage.equals(that.errorMessage) : that.errorMessage == null;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (errorMessage != null ? errorMessage.hashCode() : 0);
            result = 31 * result + (warning ? 1 : 0);
            return result;
        }

        @Override
        public String toString() {
            return "ErrorMessage{" +
                "id='" + id + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", warning=" + warning +
                '}';
        }
    }
}
