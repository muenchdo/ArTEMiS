package de.tum.in.www1.exerciseapp.domain;

import java.net.URL;

/**
 * This interface should be implemented by exercises that under the hood use a (git) repository and
 * continuous integration (CI) to save and evaluate the students submission.
 */
public interface RepositoryAndContinuousIntegrationBasedExercise {

    /**
     * Get the url of the "base" Repository that is used to clone for each student's repository
     *
     * @return The {@link URL} to the "base" repository. Not null.
     */
    public URL getBaseRepositoryUrlAsUrl();

    /**
     * The "base" build plan id that is used to "clone" the student's build plan from.
     * <p>
     * <p><b>Note:</b> This assumes that Bamboo is used as Continuous Integration System because Bamboo has a concept
     * of a buildplan id which is not necessarily the case on other CI systems.
     * Therefore, this may have to be refactored once we add support for other CI systems.</p>
     *
     * @return The base build plan id. Not null.
     */
    public String getBaseBuildPlanId();
}
