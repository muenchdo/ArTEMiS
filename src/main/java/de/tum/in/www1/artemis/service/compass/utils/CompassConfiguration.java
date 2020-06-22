package de.tum.in.www1.artemis.service.compass.utils;

/**
 * All similarity related parameters
 */
public class CompassConfiguration {

    /*
     * Similarity related parameters
     */
    // Amount that is subtracted from the similarity of two UML elements/diagrams for every missing element
    public final static double MISSING_ELEMENT_PENALTY = 0.05;

    // Weight of the relationship type when calculating the similarity of two UML relationships
    public final static double RELATION_TYPE_WEIGHT = 0.3;

    // Weight of the source and destination elements when calculating the similarity of two UML relationships
    public final static double RELATION_ELEMENT_WEIGHT = 0.25;

    // Weight of the multiplicity when calculating the similarity of two UML relationships
    public final static double RELATION_MULTIPLICITY_WEIGHT = 0.05;

    // Weight of the roles when calculating the similarity of two UML relationships
    public final static double RELATION_ROLE_WEIGHT = 0.05;

    // Weight of the class type when calculating the similarity of two UML classes
    public final static double CLASS_TYPE_WEIGHT = 0.3;

    // Weight of the class name when calculating the similarity of two UML classes
    public final static double CLASS_NAME_WEIGHT = 0.7;

    // Weight of the name similarity of their attributes when calculating the similarity of two UML classes
    public final static double ATTRIBUTE_NAME_WEIGHT = 0.7;

    // Weight of the type similarity of their attributes when calculating the similarity of two UML classes
    public final static double ATTRIBUTE_TYPE_WEIGHT = 0.3;

    // Threshold for the similarity of child elements (attributes/methods in UML classes, classes and relationships in UML diagrams) when calculation the similarity of two UML
    // classes/diagrams. If the similarity for specific child elements is smaller than the threshold, the elements will not be considered similar at all.
    public final static double NO_MATCH_THRESHOLD = 0.1;

    // Threshold used for building similarity sets. If the similarity for two UML elements is smaller than this threshold, they will not be put into the same similarity set.
    // TODO CZ: decrease equality threshold again in the future
    public final static double EQUALITY_THRESHOLD = 0.95;

    // Threshold used for re-assessing poorly assessed models. If the confidence for a model is smaller than this threshold the model is send to the client for re-assessment.
    /* CURRENTLY DISABLED */
    public final static double POORLY_ASSESSED_MODEL_THRESHOLD = 0.8;

    /*
     * Confidence and coverage parameters
     */
    // Threshold of the coverage of a UML diagram. If the coverage for a specific diagram is smaller than this threshold, no automatic result will be created for the diagram.
    /* CURRENTLY DISABLED */
    public static final double COVERAGE_THRESHOLD = 0.8;

    // Threshold of the confidence of a UML diagram. If the confidence for a specific diagram is smaller than this threshold, no automatic result will be created for the diagram.
    /* CURRENTLY DISABLED */
    public static final double CONFIDENCE_THRESHOLD = 0.75;

    // Threshold of the confidence of an element in a UML diagram. If the confidence for a specific model element is smaller than this threshold, no automatic feedback will be
    // created for this element.
    public static final double ELEMENT_CONFIDENCE_THRESHOLD = 0.8;

    /*
     * Calculation engine cleanup parameters
     */
    // Number of days to keep unused calculation engines in memory. If an engine is unused for a longer time, it will be removed in the cleanup job running every night.
    public static final int DAYS_TO_KEEP_UNUSED_ENGINE = 1;

    /*
     * Optimal model parameters
     */
    // Minimal number of optimal models that should be in cache. If the cache consists of less models, NUMBER_OF_NEW_OPTIMAL_MODELS new optimal models will be loaded.
    public static final int OPTIMAL_MODEL_THRESHOLD = 10;

    // Number of optimal models that should be generated and loaded into the cache if the number of models in the cache is smaller than OPTIMAL_MODEL_THRESHOLD.
    // NUMBER_OF_NEW_OPTIMAL_MODELS should be greater or equal to OPTIMAL_MODEL_THRESHOLD.
    public static final int NUMBER_OF_NEW_OPTIMAL_MODELS = 10;
}
