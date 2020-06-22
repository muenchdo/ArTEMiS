package de.tum.in.www1.artemis.service.compass.umlmodel;

import java.util.List;

import de.tum.in.www1.artemis.service.compass.assessment.CompassResult;

public abstract class UMLDiagram implements Similarity<UMLDiagram> {

    private long modelSubmissionId;

    private CompassResult lastAssessmentCompassResult = null;

    public UMLDiagram(long modelSubmissionId) {
        this.modelSubmissionId = modelSubmissionId;
    }

    /**
     * Gets an UML element of the UML model with the given id.
     *
     * @param jsonElementId the id of the UML element
     * @return the UML element if one could be found for the given id, null otherwise
     */
    public abstract UMLElement getElementByJSONID(String jsonElementId);

    /**
     * Get the list of first level model elements of the diagram (e.g. classes, relationships and packages of UML class diagrams, but no Attributes and Methods).
     *
     * @return the list of first level model elements of the diagram
     */
    protected abstract List<UMLElement> getModelElements();

    /**
     * Get the list of model elements of the diagram including child elements (e.g. classes, relationships and packages of UML class diagrams, including attributes and methods of
     * classes). The default behavior is to return the elements returned by getModelElements(). If the specific diagram type has elements with child elements (i.e. that are not
     * returned by getModelElements()) that need to be handled separately, the diagram type has to implement this method.
     *
     * @return the list of all model elements of the diagram including child elements
     */
    public List<UMLElement> getAllModelElements() {
        return getModelElements();
    }

    /**
     * Compares this with another diagram to calculate the similarity. It iterates over all model elements and calculates the max. similarity to elements of the reference diagram.
     * The sum of the weighted single element similarity scores is the total similarity score of the two diagrams.
     *
     * @param reference the reference UML diagram to compare this diagram with
     * @return the similarity of the diagrams as number [0-1]
     */
    @Override
    public double similarity(Similarity<UMLDiagram> reference) {
        if (reference == null || !reference.getClass().isInstance(this)) {
            return 0;
        }

        UMLDiagram diagramReference = (UMLDiagram) reference;

        // To ensure symmetry (i.e. A.similarity(B) = B.similarity(A)) we make sure that this diagram always has less or equally many elements than the reference diagram.
        if (getModelElements().size() > diagramReference.getModelElements().size()) {
            return diagramReference.similarity(this);
        }

        double similarity = 0;

        // For calculating the weight of the similarity of every element, we consider the max. element count to reflect missing elements, i.e. it should not be possible to get a
        // similarity of 1 if the amount of elements differs. As we know that the reference diagram has at least as many elements as this diagram, we take the element count of the
        // reference.
        int maxElementCount = diagramReference.getModelElements().size();
        double weight = 1.0 / maxElementCount;

        for (Similarity<UMLElement> element : getModelElements()) {
            double similarityValue = diagramReference.similarElementScore(element);
            similarity += weight * similarityValue;
        }

        // Make sure that the similarity value is between 0 and 1.
        return Math.min(Math.max(similarity, 0), 1);
    }

    /**
     * Compares a reference element to the list of model elements of this diagram and returns the maximum similarity score, i.e. the similarity between the reference element and
     * the most similar element of this diagram.
     *
     * @param referenceElement the reference element that should be compared to the model elements of this diagram
     * @return the maximum similarity score of the reference element and the list of model elements of this diagram
     */
    private double similarElementScore(Similarity<UMLElement> referenceElement) {
        return getModelElements().stream().mapToDouble(element -> element.overallSimilarity(referenceElement)).max().orElse(0);
    }

    /**
     * Return the submissionId of the UML diagram.
     *
     * @return the submissionId of the UML diagram
     */
    public long getModelSubmissionId() {
        return modelSubmissionId;
    }

    /**
     * Set the lastAssessmentCompassResult that represents the most recent automatic assessment calculated by Compass for this diagram.
     *
     * @param compassResult the most recent Compass result for this diagram
     */
    public void setLastAssessmentCompassResult(CompassResult compassResult) {
        lastAssessmentCompassResult = compassResult;
    }

    /**
     * Returns the lastAssessmentCompassResult that represents the most recent automatic assessment calculated by Compass for this diagram.
     *
     * @return the most recent Compass result for this diagram
     */
    public CompassResult getLastAssessmentCompassResult() {
        return lastAssessmentCompassResult;
    }

    /**
     * Indicates if this diagram already has an automatic assessment calculated by Compass or not.
     *
     * @return true if Compass has not already calculated an automatic assessment for this diagram, false otherwise
     */
    public boolean isUnassessed() {
        return lastAssessmentCompassResult == null;
    }

    /**
     * Get the confidence of the last compass result, i.e. the most recent automatic assessment calculated by Compass for this diagram.
     *
     * @return The confidence of the last compass result, -1 if no compass result is available
     */
    public double getLastAssessmentConfidence() {
        if (isUnassessed()) {
            return -1;
        }

        return lastAssessmentCompassResult.getConfidence();
    }

    /**
     * Get the coverage for the last assessed compass result, i.e. the most recent automatic assessment calculated by Compass for this diagram.
     *
     * @return The coverage of the last compass result, -1 if no compass result is available
     */
    public double getLastAssessmentCoverage() {
        if (isUnassessed()) {
            return -1;
        }

        return lastAssessmentCompassResult.getCoverage();
    }

    /**
     * Get a human readable name of this diagram in the form "Model <submissionId>".
     *
     * @return a human readable name of this diagram in the form "Model <submissionId>"
     */
    public String getName() {
        return "Model " + modelSubmissionId;
    }
}
